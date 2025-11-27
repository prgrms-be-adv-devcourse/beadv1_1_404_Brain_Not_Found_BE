package com.ll.order.domain.service;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.vo.request.*;
import com.ll.order.domain.model.vo.response.cart.CartItemInfo;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import com.ll.order.domain.model.vo.response.order.*;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final CartServiceClient cartServiceClient;
    private final PaymentServiceClient paymentApiClient;

    private final OrderEventProducer orderEventProducer;
    private final OrderValidator orderValidator;

    @Override
    public OrderPageResponse findAllOrders(String userCode, String keyword, Pageable pageable) {
        UserResponse userInfo = getUserInfo(userCode);

        // keyword가 있으면 상품명으로 검색, 없으면 전체 조회
        Page<Order> orderPage;
        if (keyword != null && !keyword.isBlank()) {
            orderPage = orderJpaRepository.findByBuyerIdAndProductNameContaining(
                    userInfo.id(), keyword, pageable);
        } else {
            orderPage = orderJpaRepository.findByBuyerId(userInfo.id(), pageable);
        }

        return OrderPageResponse.from(orderPage);
    }

    @Override
    public OrderDetailResponse findOrderDetails(String orderCode) {
        Order order = findOrderByCode(orderCode);

        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId()); // 쿼리 날리는지 디버깅 해보도록.
        List<OrderDetailResponse.ItemInfo> itemInfos = orderItems.stream()
                .map(item -> OrderDetailResponse.ItemInfo.from(item, getProductInfo(item.getProductCode())))
                .collect(Collectors.toList());

        return OrderDetailResponse.from(order, itemInfos);
    }

    @Override
    @Transactional
    public OrderCreateResponse createCartItemOrder(OrderCartItemRequest request, String userCode) {
        UserResponse userInfo = getUserInfo(userCode);

        // 주문 및 주문 상품 생성 (독립 트랜잭션 - 항상 커밋)
        OrderCreationResult creationResult = createOrderWithItems(request, userInfo);
        Order savedOrder = creationResult.order();
        List<OrderItem> orderItems = creationResult.orderItems();

        // 결제 처리 (별도 트랜잭션)
        if (request.paidType() == PaidType.DEPOSIT) {
            processDepositPayment(savedOrder, orderItems, request);
        } else if (request.paidType() == PaidType.TOSS_PAYMENT) {
            // 토스 결제: 주문만 생성하고 결제는 UI에서 처리
            // 주문 상태는 CREATED로 유지 (결제 완료 후 completePaymentWithKey에서 COMPLETED로 변경)
            log.info("토스 결제 주문 생성 완료 - orderId: {}, orderCode: {}, 상태: CREATED (결제 대기)",
                    savedOrder.getId(), savedOrder.getCode());
        } else {
            log.warn("지원하지 않는 결제 수단입니다. paidType: {}", request.paidType());
            throw new BaseException(OrderErrorCode.UNSUPPORTED_PAYMENT_TYPE);
        }

        return convertToOrderCreateResponse(savedOrder);
    }

    //     주문 및 주문 상품 생성 (독립 트랜잭션)
//     결제 실패와 무관하게 주문은 항상 저장됨
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderCreationResult createOrderWithItems(OrderCartItemRequest request, UserResponse userInfo) {
        CartItemsResponse cartInfo = getCartInfo(request.cartCode());

        List<ProductResponse> productList = new ArrayList<>();
        for (CartItemInfo item : cartInfo.items()) {
            ProductResponse productInfo = getProductInfo(item.productCode());
            productList.add(productInfo);
        }

        Order order = Order.create(
                userInfo.id(),
                userInfo.code(),
                request.orderType(),
                request.address()
        );
        Order savedOrder = orderJpaRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemInfo cartItem : cartInfo.items()) {
            ProductResponse productInfo = productList.stream()
                    .filter(response -> response.id().equals(cartItem.productId()))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("상품을 찾을 수 없습니다. productId: {}", cartItem.productId());
                        return new BaseException(OrderErrorCode.PRODUCT_NOT_FOUND);
                    });

            OrderItem orderItem = savedOrder.createOrderItem(
                    productInfo.id(),
                    productInfo.code(),
                    productInfo.sellerCode(),
                    productInfo.name(),
                    cartItem.quantity(),
                    productInfo.price()
            );
            orderItems.add(orderItem);
        }

        orderItemJpaRepository.saveAll(orderItems);

        return new OrderCreationResult(savedOrder, orderItems);
    }

    /**
     * 예치금 결제 처리 (별도 트랜잭션)
     * 실패해도 주문에는 영향 없음 (주문은 이미 커밋됨)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDepositPayment(Order order, List<OrderItem> orderItems, OrderCartItemRequest request) {
        OrderPaymentRequest orderPaymentRequest = OrderPaymentRequest.from(
                order,
                order.getBuyerCode(),
                request.paidType(),
                request.paymentKey()
        );

        try {
            paymentApiClient.requestDepositPayment(orderPaymentRequest);

            order.changeStatus(OrderStatus.COMPLETED);
            orderJpaRepository.save(order);

            publishOrderCompletedEvents(order, orderItems, order.getBuyerCode());

            log.info("예치금 결제 완료 - orderCode: {}, amount: {}",
                    order.getCode(), order.getTotalPrice());
        } catch (Exception e) {
            order.changeStatus(OrderStatus.FAILED);
            orderJpaRepository.save(order);

            log.error("결제 처리 실패 - orderCode: {}, error: {}",
                    order.getCode(), e.getMessage(), e);

            throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
        }
    }


    @Override
    public OrderCreateResponse createDirectOrder(OrderDirectRequest request, String userCode) {
        UserResponse userInfo = getUserInfo(userCode);

        // 주문 및 주문 상품 생성 (독립 트랜잭션 - 항상 커밋)
        OrderCreationResult creationResult = createDirectOrderWithItem(request, userInfo);
        Order savedOrder = creationResult.order();
        List<OrderItem> orderItems = creationResult.orderItems();

        // 결제 처리 (별도 트랜잭션)
        if (request.paidType() == PaidType.DEPOSIT) {
            processDirectDepositPayment(savedOrder, orderItems, request);
        } else if (request.paidType() == PaidType.TOSS_PAYMENT) {
            // 토스 결제: 주문만 생성하고 결제는 UI에서 처리
            // 주문 상태는 CREATED로 유지 (결제 완료 후 completePaymentWithKey에서 COMPLETED로 변경)
            log.info("토스 결제 주문 생성 완료 - orderId: {}, orderCode: {}, 상태: CREATED (결제 대기)",
                    savedOrder.getId(), savedOrder.getCode());
        } else {
            log.warn("지원하지 않는 결제 수단입니다. paidType: {}", request.paidType());
            throw new BaseException(OrderErrorCode.UNSUPPORTED_PAYMENT_TYPE);
        }

        return convertToOrderCreateResponse(savedOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderCreationResult createDirectOrderWithItem(OrderDirectRequest request, UserResponse userInfo) {
        ProductResponse productInfo = getProductInfo(request.productCode());

        log.info("상품 정보 조회 완료 - id: {}, code: {}, name: {}, sellerCode: {}, sellerName: {}, quantity: {}, price: {}, status: {}",
                productInfo.id(),
                productInfo.code(),
                productInfo.name(),
                productInfo.sellerCode(),
                productInfo.sellerName(),
                productInfo.quantity(),
                productInfo.price(),
                productInfo.status());

        Order order = Order.create(
                userInfo.id(),
                userInfo.code(),
                request.orderType(),
                request.address()
        );
        Order savedOrder = orderJpaRepository.save(order);

        OrderItem orderItem = savedOrder.createOrderItem(
                productInfo.id(),
                productInfo.code(),
                productInfo.sellerCode(),
                productInfo.name(),
                request.quantity(),
                productInfo.price()
        );
        orderItemJpaRepository.save(orderItem);

        return new OrderCreationResult(savedOrder, List.of(orderItem));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDirectDepositPayment(Order order, List<OrderItem> orderItems, OrderDirectRequest request) {
        OrderPaymentRequest orderPaymentRequest = OrderPaymentRequest.from(
                order,
                order.getBuyerCode(),
                request.paidType(),
                request.paymentKey()
        );

        try {
            paymentApiClient.requestDepositPayment(orderPaymentRequest);

            order.changeStatus(OrderStatus.COMPLETED);
            orderJpaRepository.save(order);

            publishOrderCompletedEvents(order, orderItems, order.getBuyerCode());

            log.info("예치금 결제 완료 - orderCode: {}, amount: {}",
                    order.getCode(), order.getTotalPrice());
        } catch (Exception e) {
            order.changeStatus(OrderStatus.FAILED);
            orderJpaRepository.save(order);

            log.error("결제 처리 실패 - orderCode: {}, error: {}",
                    order.getCode(), e.getMessage(), e);

            throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
        }
    }

    @Override
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(String orderCode, @Valid OrderStatusUpdateRequest request, String userCode) {
        Order order = findOrderByCode(orderCode);

        OrderStatus current = order.getOrderStatus();
        OrderStatus target = request.status();

        orderValidator.validateOrderStatusTransition(current, target);

        if (target == OrderStatus.CANCELLED) {
            handleOrderCancellation(order);
        }

        order.changeStatus(target);
        orderJpaRepository.save(order);

        return new OrderStatusUpdateResponse(
                order.getCode(),
                order.getOrderStatus(),
                order.getUpdatedAt()
        );
    }

    @Override
    @Transactional
    public void completePaymentWithKey(String orderCode, String paymentKey) {
        Order order = findOrderByCode(orderCode);

        if (order.getOrderStatus() != OrderStatus.CREATED) {
            log.warn("이미 처리된 주문입니다. orderCode: {}, 현재 상태: {}", orderCode, order.getOrderStatus());
            throw new BaseException(OrderErrorCode.ORDER_ALREADY_PROCESSED);
        }

        OrderPaymentRequest orderPaymentRequest = OrderPaymentRequest.from(
                order,
                order.getBuyerCode(),
                PaidType.TOSS_PAYMENT,
                paymentKey
        );

        try {
            paymentApiClient.requestTossPayment(orderPaymentRequest);

            order.changeStatus(OrderStatus.COMPLETED);
            orderJpaRepository.save(order);

            // 결제 성공 후 이벤트 발행
            List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
            publishOrderCompletedEvents(order, orderItems, order.getBuyerCode());

        } catch (Exception e) {
            order.changeStatus(OrderStatus.FAILED);
            orderJpaRepository.save(order);
            log.error("결제 처리 실패 - orderId: {}, paymentKey: {}, error: {}", orderCode, paymentKey, e.getMessage(), e);
            throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getOrderCodeById(Long orderId) {
        Order order = orderJpaRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("주문을 찾을 수 없습니다. orderId: {}", orderId);
                    return new BaseException(OrderErrorCode.ORDER_NOT_FOUND);
                });
        return order.getCode();
    }

    @Override
    public Optional<String> buildPaymentRedirectUrl(OrderCreateResponse response, PaidType paidType) {
        if (paidType != PaidType.TOSS_PAYMENT) {
            return Optional.empty();
        }

        String orderName = "주문번호: " + response.orderCode();
        String redirectUrl = String.format("/orders/payment?orderId=%d&orderName=%s&amount=%d",
                response.id(),
                URLEncoder.encode(orderName, StandardCharsets.UTF_8),
                response.totalPrice());
        return Optional.of(redirectUrl);
    }

    @Override
    public OrderValidateResponse validateOrder(OrderValidateRequest request) {
        List<OrderValidateResponse.ItemInfo> itemInfos = orderValidator.validateProducts(request.products());

        int totalQuantity = itemInfos.stream()
                .mapToInt(OrderValidateResponse.ItemInfo::requestedQuantity)
                .sum();

        long totalAmount = itemInfos.stream()
                .mapToLong(item -> (long) item.price() * item.requestedQuantity())
                .sum();

        return OrderValidateResponse.from(
                request.buyerCode(),
                totalQuantity,
                totalAmount,
                itemInfos
        );
    }

    // 주문 취소 처리 -> 환불 처리(동기) + 환불 이벤트 발행(비동기) + 재고 복구 요청
    private void handleOrderCancellation(Order order) {
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
        String buyerCode = order.getBuyerCode();

        // 1. 환불 처리 (동기) - 결제가 완료된 주문만 환불 처리
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            try {
                paymentApiClient.requestRefund(
                        order.getId(),
                        order.getCode(),
                        buyerCode,
                        order.getTotalPrice(),
                        "주문 취소"
                );
                log.info("환불 처리 완료 - orderCode: {}, amount: {}", order.getCode(), order.getTotalPrice());
            } catch (Exception e) {
                log.error("환불 처리 실패 - orderCode: {}, error: {}", order.getCode(), e.getMessage(), e);
                throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
            }
        }

        // 2. 환불 이벤트 발행 (비동기) + 재고 복구 요청
        for (OrderItem orderItem : orderItems) {
            if (buyerCode != null) {
                RefundEvent refundEvent = RefundEvent.from(
                        buyerCode,
                        orderItem.getCode(),
                        order.getCode(),
                        (long) orderItem.getPrice() * orderItem.getQuantity()
                );
                orderEventProducer.sendRefund(refundEvent);
                log.info("Refund event sent - orderCode: {}, orderItemCode: {}, amount: {}",
                        order.getCode(), orderItem.getCode(), refundEvent.amount());
            }

            try {
                productServiceClient.restoreInventory(orderItem.getProductCode(), orderItem.getQuantity());
                log.info("재고 복구 요청 - productCode: {}, quantity: {}",
                        orderItem.getProductCode(), orderItem.getQuantity());
            } catch (Exception e) {
                log.error("재고 복구 요청 실패 - productCode: {}, quantity: {}, error: {}",
                        orderItem.getProductCode(), orderItem.getQuantity(), e.getMessage(), e);
            }
        }
    }

    private ProductResponse getProductInfo(String productCode) {
        return Optional.ofNullable(productServiceClient.getProductByCode(productCode))
                .orElseThrow(() -> {
                    log.warn("상품을 찾을 수 없습니다. productCode: {}", productCode);
                    return new BaseException(OrderErrorCode.PRODUCT_NOT_FOUND);
                });
    }

    private OrderCreateResponse convertToOrderCreateResponse(Order order) {
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
        return OrderCreateResponse.from(order, orderItems);
    }

    private UserResponse getUserInfo(String userCode) {
        return Optional.ofNullable(userServiceClient.getUserByCode(userCode))
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없습니다. userCode: {}", userCode);
                    return new BaseException(OrderErrorCode.USER_NOT_FOUND);
                });
    }

    private CartItemsResponse getCartInfo(String cartCode) {
        CartItemsResponse cartInfo = Optional.ofNullable(cartServiceClient.getCartByCode(cartCode))
                .orElseThrow(() -> {
                    log.warn("장바구니를 찾을 수 없습니다. cartCode: {}", cartCode);
                    return new BaseException(OrderErrorCode.CART_NOT_FOUND);
                });

        if (cartInfo.isEmpty()) {
            log.warn("장바구니가 비어있습니다. cartCode: {}", cartCode);
            throw new BaseException(OrderErrorCode.CART_EMPTY);
        }

        return cartInfo;
    }

    private Order findOrderByCode(String orderCode) {
        return Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                .orElseThrow(() -> {
                    log.warn("주문을 찾을 수 없습니다. orderCode: {}", orderCode);
                    return new BaseException(OrderErrorCode.ORDER_NOT_FOUND);
                });
    }

    // 주문 완료 후 이벤트 발행 - 정산 이벤트 (order-event) - 재고 감소 이벤트 (inventory-event)
    private void publishOrderCompletedEvents(Order order, List<OrderItem> orderItems, String buyerCode) {
        for (OrderItem orderItem : orderItems) {
            OrderEvent orderEvent = OrderEvent.of(
                    buyerCode,
                    orderItem.getSellerCode(),
                    orderItem.getCode(),
                    order.getCode(),
                    (long) orderItem.getPrice() * orderItem.getQuantity()
            );
            orderEventProducer.sendOrder(orderEvent);
            log.info("주문 이벤트 send - orderCode: {}, orderItemCode: {}", order.getCode(), orderItem.getCode());

            // 재고 감소 이벤트 발행
            orderEventProducer.sendInventoryDecrease(
                    String.valueOf(orderItem.getCode()),
                    orderItem.getQuantity()
            );
            log.info("Inventory decrease event sent - productId: {}, quantity: {}", orderItem.getProductId(), orderItem.getQuantity());
        }
    }

    // 도메인 별 규칙에 맞는 검증 로직이 필요하면 추가 작성할 것.
}
