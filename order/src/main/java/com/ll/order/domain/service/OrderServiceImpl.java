package com.ll.order.domain.service;

import com.ll.cart.model.vo.response.CartItemInfo;
import com.ll.cart.model.vo.response.CartItemsResponse;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.vo.request.*;
import com.ll.order.domain.model.vo.response.*;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.products.domain.product.model.dto.response.ProductResponse;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.user.model.vo.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    @Transactional(readOnly = true)
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

        // Order 엔티티를 OrderInfo DTO로 변환
        List<OrderPageResponse.OrderInfo> orderInfoList = orderPage.getContent().stream()
                .map(order -> new OrderPageResponse.OrderInfo(
                        order.getId(),
                        order.getCode(),
                        order.getOrderStatus(),
                        order.getTotalPrice(),
                        new OrderPageResponse.UserInfo(
                                order.getBuyerId(),
                                order.getAddress()
                        )
                ))
                .collect(Collectors.toList());

        OrderPageResponse.PageInfo pageInfo = new OrderPageResponse.PageInfo(
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.hasNext(),
                orderPage.hasPrevious()
        );

        return new OrderPageResponse(orderInfoList, pageInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse findOrderDetails(String orderCode) {
        // 명확한 행위 메서드인데, 예외를 던지는 것들은 전부 repo impl같은 곳에 모아두는 걸 추천
        Order order = Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderCode));

        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId()); // 쿼리 날리는지 디버깅 해보도록.
        List<OrderDetailResponse.ItemInfo> itemInfos = orderItems.stream()
                .map(item -> {
                    ProductResponse product = Optional.ofNullable(productServiceClient.getProductById(item.getProductId()))
                            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + item.getProductId()));
                    // images가 비어있지 않으면 첫 번째 이미지의 URL 사용, 없으면 null
                    String productImage = product.images() != null && !product.images().isEmpty()
                            ? product.images().get(0).url()
                            : null;
                    return new OrderDetailResponse.ItemInfo(
                            item.getCode(),
                            item.getProductId(),
                            item.getSellerCode(),
                            item.getProductName(),
                            item.getQuantity(),
                            item.getPrice(),
                            productImage
                    );
                })
                .collect(Collectors.toList());

        // 생성자 파라미터안에 생성자가 있는 구조는 가독성 측면에서 생각해봐야한다고 봅니다! 인스턴스화해서 변수로 활용하는 게 어떨까요? 넵.
        OrderDetailResponse.UserInfo userInfo = new OrderDetailResponse.UserInfo(
                order.getBuyerId(),
                order.getAddress()
        );

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                userInfo,
                itemInfos
        );
    }

    @Override
    @Transactional
    public OrderCreateResponse createCartItemOrder(OrderCartItemRequest request) {
        UserResponse userInfo = getUserInfo(request.buyerCode());

        CartItemsResponse cartInfo = getCartInfo(request.cartCode());

        if (cartInfo.items() == null || cartInfo.items().isEmpty()) { // 이것도 response 안에 행위로 둘 것 같음.
            throw new IllegalArgumentException("장바구니가 비어있습니다: " + request.cartCode());
        }

        //Map 보다 List<객체> 형식으로 사용하는 게 좀 더 확장성과 객체지향적이라고 생각합니다.
        List<ProductResponse> productList = new ArrayList<>();
        for (CartItemInfo item : cartInfo.items()) {
            ProductResponse productInfo = getProductInfoById(item.productId());
            productList.add(productInfo);
        }

        Order order = Order.create(
                userInfo.id(),
                request.orderType(),
                request.address()
        );
        Order savedOrder = orderJpaRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemInfo cartItem : cartInfo.items()) {
            ProductResponse productInfo = productList.stream()
                    .filter(p -> p.id().equals(cartItem.productId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + cartItem.productId()));

            OrderItem orderItem = savedOrder.createOrderItem(
                    productInfo.id(),
                    productInfo.sellerCode(),
                    productInfo.name(),
                    cartItem.quantity(),
                    productInfo.price()
            );
            orderItems.add(orderItem);
        }

        orderItemJpaRepository.saveAll(orderItems);
        /*
        1) OrderCartItemRequest → 사용자 입력값 받기
        2) OrderServiceImpl에서 PaidType 유효성 검사
        3) OrderPaymentRequest에 PaidType 포함 → 결제 도메인 전달
        */
        OrderPaymentRequest orderPaymentRequest = new OrderPaymentRequest(
                savedOrder.getId(),
                savedOrder.getBuyerId(),
                request.buyerCode(),
                savedOrder.getTotalPrice(),
                request.paidType(),
                request.paymentKey()
        );

        // 결제 도메인 호출
        try {
            switch (request.paidType()) {
                case DEPOSIT -> paymentApiClient.requestDepositPayment(orderPaymentRequest);
                case TOSS_PAYMENT -> paymentApiClient.requestTossPayment(orderPaymentRequest);
                default -> throw new IllegalArgumentException("지원하지 않는 결제 수단입니다: " + request.paidType());
            }

            // 결제 성공 후
            savedOrder.changeStatus(OrderStatus.COMPLETED);
            orderJpaRepository.save(savedOrder);

            publishOrderCompletedEvents(savedOrder, orderItems, request.buyerCode());

        } catch (Exception e) {
            // 결제 실패 시 주문 상태를 FAILED로 변경
            savedOrder.changeStatus(OrderStatus.FAILED);
            orderJpaRepository.save(savedOrder);
            log.error("결제 처리 실패 - orderCode: {}, error: {}", savedOrder.getCode(), e.getMessage(), e);
            throw new IllegalStateException("결제 처리에 실패했습니다: " + e.getMessage(), e);
        }

        return convertToOrderCreateResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderCreateResponse createDirectOrder(OrderDirectRequest request) {
        UserResponse userInfo = getUserInfo(request.userCode());

        ProductResponse productInfo = getProductInfo(request.productCode());

        log.info("상품 정보 조회 완료 - id: {}, code: {}, name: {}, sellerCode: {}, sellerName: {}, quantity: {}, price: {}, status: {}, images: {}",
                productInfo.id(),
                productInfo.code(),
                productInfo.name(),
                productInfo.sellerCode(),
                productInfo.sellerName(),
                productInfo.quantity(),
                productInfo.price(),
                productInfo.status(),
                productInfo.images());

        Order order = Order.create(
//                1L,
                userInfo.id(),
                request.orderType(),
                request.address()
        );
        Order savedOrder = orderJpaRepository.save(order);

        OrderItem orderItem = savedOrder.createOrderItem(
                productInfo.id(),
                productInfo.sellerCode(),
                productInfo.name(),
                request.quantity(),
                productInfo.price()
        );
        orderItemJpaRepository.save(orderItem);

        OrderPaymentRequest orderPaymentRequest = new OrderPaymentRequest(
                savedOrder.getId(),
                savedOrder.getBuyerId(),
                request.userCode(),
                savedOrder.getTotalPrice(),
                request.paidType(),
                request.paymentKey()
        );

        try {
            switch (request.paidType()) {
                case DEPOSIT -> paymentApiClient.requestDepositPayment(orderPaymentRequest);
                case TOSS_PAYMENT -> paymentApiClient.requestTossPayment(orderPaymentRequest);
                default -> throw new IllegalArgumentException("지원하지 않는 결제 수단입니다: " + request.paidType());
            }

            // 결제 성공 후
            savedOrder.changeStatus(OrderStatus.COMPLETED);
            orderJpaRepository.save(savedOrder);

            // 결제 성공 후 이벤트 발행
            List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(savedOrder.getId());
            publishOrderCompletedEvents(savedOrder, orderItems, request.userCode());

        } catch (Exception e) {
            // 결제 실패 시
            savedOrder.changeStatus(OrderStatus.FAILED);
            orderJpaRepository.save(savedOrder);
            log.error("결제 처리 실패 - orderCode: {}, error: {}", savedOrder.getCode(), e.getMessage(), e);
            throw new IllegalStateException("결제 처리에 실패했습니다: " + e.getMessage(), e);
        }

        return convertToOrderCreateResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderStatusUpdateResponse updateOrderStatus(String orderCode, @Valid OrderStatusUpdateRequest request) {
        Order order = Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderCode));

        OrderStatus current = order.getOrderStatus();
        OrderStatus target = request.status();

        if (!current.canTransitionTo(target)) {
            throw new IllegalStateException("해당 상태로 전환할 수 없습니다: " + current + " -> " + target);
        }

        // 주문 취소 시 추가 로직 처리
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
    public OrderValidateResponse validateOrder(OrderValidateRequest request) {
//        UserResponse userInfo = getUserInfo(request.buyerCode());

        Set<String> duplicatedCheck = new HashSet<>();
        int totalQuantity = 0;
        long totalAmount = 0L;
        List<OrderValidateResponse.ItemInfo> itemInfos = new ArrayList<>();

        for (ProductRequest productRequest : request.products()) {
            if (!duplicatedCheck.add(productRequest.productCode())) { // 이미 들어있는 코드면 false 반환
                throw new IllegalArgumentException("중복된 상품 코드가 포함되어 있습니다: " + productRequest.productCode());
            }

            ProductResponse productInfo = getProductInfo(productRequest.productCode());

            if (productInfo.quantity() < productRequest.quantity()) {
                throw new IllegalStateException("재고가 부족합니다. 상품 코드: " + productRequest.productCode());
            }

            if (productInfo.price() <= 0) {
                throw new IllegalStateException("유효하지 않은 상품 가격입니다. 상품 코드: " + productRequest.productCode());
            }

            if (productInfo.status() == null || productInfo.status() != ProductStatus.ON_SALE) {
                throw new IllegalStateException("판매 중이 아닌 상품입니다. 상품 코드: " + productRequest.productCode());
            }

            if (productRequest.price() != productInfo.price()) {
                throw new IllegalStateException("요청한 상품 가격이 실제 가격과 일치하지 않습니다. 상품 코드: " + productRequest.productCode());
            }

            totalQuantity += productRequest.quantity();
            totalAmount += (long) productInfo.price() * productRequest.quantity();

            itemInfos.add(new OrderValidateResponse.ItemInfo(
                    productRequest.productCode(),
                    productRequest.quantity(),
                    productInfo.price()
            ));
        }

        if (totalQuantity <= 0) {
            throw new IllegalArgumentException("주문 수량이 0 이하입니다.");
        }

        if (totalAmount <= 0) {
            throw new IllegalArgumentException("주문 금액이 0 이하입니다.");
        }

        return new OrderValidateResponse(
                request.buyerCode(),
                totalQuantity,
                BigDecimal.valueOf(totalAmount),
                itemInfos
        );
    }

    /**
     * 주문 취소 처리
     * - 환불 이벤트 발행
     * - 재고 복구 요청
     */
    private void handleOrderCancellation(Order order) {
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());

        UserResponse buyerInfo = getUserInfoById(order.getBuyerId());
        String buyerCode = buyerInfo != null ? buyerInfo.code() : null;
        if (buyerCode == null) {
            log.warn("주문 취소 시 buyerCode를 찾을 수 없습니다. orderCode: {}, buyerId: {}",
                    order.getCode(), order.getBuyerId());
        }

        for (OrderItem orderItem : orderItems) {
            // 환불 이벤트 발행
            if (buyerCode != null) {
                RefundEvent refundEvent = new RefundEvent(
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
                ProductResponse productInfo = getProductInfoById(orderItem.getProductId());
                if (productInfo != null && productInfo.code() != null) {
                    productServiceClient.restoreInventory(productInfo.code(), orderItem.getQuantity());
                    log.info("재고 복구 요청 - productCode: {}, quantity: {}",
                            productInfo.code(), orderItem.getQuantity());
                } else {
                    log.warn("재고 복구 실패 - 상품 정보를 찾을 수 없습니다. productId: {}", orderItem.getProductId());
                }
            } catch (Exception e) {
                log.error("재고 복구 요청 실패 - productId: {}, quantity: {}, error: {}",
                        orderItem.getProductId(), orderItem.getQuantity(), e.getMessage(), e);
            }
        }
    }

    /**
     * 사용자 ID로 사용자 정보 조회
     */
    private UserResponse getUserInfoById(Long userId) {
        try {
            return userServiceClient.getUserById(userId);
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return null;
        }
    }

    private ProductResponse getProductInfo(String productCode) {
        return Optional.ofNullable(productServiceClient.getProductByCode(productCode))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productCode));
    }

    private ProductResponse getProductInfoById(Long productId) {
        return Optional.ofNullable(productServiceClient.getProductById(productId))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
    }

    private OrderCreateResponse convertToOrderCreateResponse(Order order) {
        List<OrderCreateResponse.OrderItemInfo> orderItemInfoList = orderItemJpaRepository.findByOrderId(order.getId()).stream()
                .map(item -> new OrderCreateResponse.OrderItemInfo(
                        item.getId(),
                        item.getCode(),
                        item.getProductId(),
                        item.getSellerCode(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        return new OrderCreateResponse(
                order.getId(),
                order.getCode(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getOrderType(),
                order.getAddress(),
                order.getBuyerId(),
                orderItemInfoList
        );
    }

    private UserResponse getUserInfo(String userCode) {
        return Optional.ofNullable(userServiceClient.getUserByCode(userCode))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userCode));
    }

    private CartItemsResponse getCartInfo(String cartCode) {
        return Optional.ofNullable(cartServiceClient.getCartByCode(cartCode))
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다: " + cartCode));
    }

    /**
     * 주문 완료 후 이벤트 발행
     * - 정산 이벤트 (order-event)
     * - 재고 감소 이벤트 (inventory-event)
     */
    private void publishOrderCompletedEvents(Order order, List<OrderItem> orderItems, String buyerCode) {
        for (OrderItem orderItem : orderItems) {
            // 정산을 위한 주문 이벤트 발행
            // OrderEvent.of(buyerCode, sellerCode, orderItemCode, referenceCode, amount)
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
//            orderEventProducer.sendInventoryDecrease(
//                    String.valueOf(orderItem.getCode()),
//                    orderItem.getQuantity()
//            );
//            log.info("Inventory decrease event sent - productId: {}, quantity: {}", orderItem.getProductId(), orderItem.getQuantity());
        }
    }

    // 도메인 별 규칙에 맞는 검증 로직이 필요하면 추가 작성할 것.
}
