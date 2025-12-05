package com.ll.order.domain.service.order;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.order.domain.client.*;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.entity.history.OrderHistoryBuilder;
import com.ll.order.domain.model.entity.history.OrderHistoryEntity;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.vo.request.*;
import com.ll.order.domain.model.vo.response.order.*;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderHistoryJpaRepository;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.domain.service.compensation.CompensationService;
import com.ll.order.domain.service.event.OrderEventService;
import com.ll.order.domain.service.inventory.OrderInventoryService;
import com.ll.order.domain.service.order.create.strategy.CartOrderCreationStrategy;
import com.ll.order.domain.service.order.create.strategy.DirectOrderCreationStrategy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
    private final OrderHistoryJpaRepository orderHistoryJpaRepository;

    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final PaymentServiceClient paymentApiClient;

    private final OrderEventProducer orderEventProducer;
    private final OrderValidator orderValidator;
    
    private final CompensationService compensationService;
    private final OrderEventService orderEventService;
    private final OrderInventoryService orderInventoryService;

    // Strategy 패턴을 위한 주문 생성 전략들
    private final CartOrderCreationStrategy cartOrderCreationStrategy;
    private final DirectOrderCreationStrategy directOrderCreationStrategy;

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
    public OrderCreateResponse createCartItemOrder(OrderCartItemRequest request, String userCode) {
        return cartOrderCreationStrategy.createOrder(request, userCode);
    }

    @Override
    public OrderCreateResponse createDirectOrder(OrderDirectRequest request, String userCode) {
        return directOrderCreationStrategy.createOrder(request, userCode);
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

        // 주문 상태 변경 이력 저장
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
        String reason = target == OrderStatus.CANCELLED ? "주문 취소" : "주문 상태 변경";
        OrderHistoryEntity statusHistory = OrderHistoryBuilder.createStatusChangeHistory(
                order, orderItems, current, reason, userCode);
        orderHistoryJpaRepository.save(statusHistory);

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

        OrderStatus previousStatus = order.getOrderStatus();

        try {
            paymentApiClient.requestTossPayment(orderPaymentRequest);

            order.changeStatus(OrderStatus.COMPLETED);
            orderJpaRepository.save(order);

            // 주문 상태 변경 이력 저장 (결제 성공)
            List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
            OrderHistoryEntity successHistory = OrderHistoryBuilder.createPaymentSuccessHistory(
                    order, orderItems, previousStatus, "토스");
            orderHistoryJpaRepository.save(successHistory);

            // 주문 완료 이벤트 발행 (주문 상태가 COMPLETED일 때)
            orderEventService.publishOrderCompletedEvents(order, orderItems, order.getBuyerCode());

        } catch (Exception e) {
            order.changeStatus(OrderStatus.FAILED);
            orderJpaRepository.save(order);

            // 주문 상태 변경 이력 저장 (결제 실패)
            List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
            OrderHistoryEntity failHistory = OrderHistoryBuilder.createPaymentFailHistory(
                    order, orderItems, previousStatus, "토스", e.getMessage());
            orderHistoryJpaRepository.save(failHistory);

            // 결제 실패 시 재고 롤백 (재고 차감이 주문 생성 시점에 이루어졌기 때문)
            orderInventoryService.rollbackInventoryForOrder(orderItems, order.getCode());

            log.error("결제 처리 실패 - orderId: {}, paymentKey: {}, error: {}", orderCode, paymentKey, e.getMessage(), e);
            throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
        }
    }

    @Override
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

    // 주문 취소 처리 -> 환불 처리(동기) + 환불 이벤트 발행(비동기) + 재고 복구 요청
    // 부모 트랜잭션 ( updateOrderStatus ) 에서 호출되는 메서드
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
                log.debug("환불 처리 완료 - orderCode: {}, amount: {}", order.getCode(), order.getTotalPrice());
            } catch (Exception e) {
                String errorMessage = String.format("환불 처리 실패 - orderCode: %s, error: %s",
                        order.getCode(), e.getMessage());
                log.error(errorMessage, e);
                // 보상 로직 실패 시 TransactionTracing에 실패 상태 저장
                compensationService.markCompensationFailed(order.getCode(), errorMessage);
                throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
            }
        }

        // 2. 환불 이벤트 발행 (비동기) + 재고 복구 이벤트 발행 (비동기)
        for (OrderItem orderItem : orderItems) {
            if (buyerCode != null) {
                RefundEvent refundEvent = RefundEvent.from(
                        buyerCode,
                        orderItem.getCode(),
                        order.getCode(),
                        (long) orderItem.getPrice() * orderItem.getQuantity()
                );
                orderEventProducer.sendRefund(refundEvent);
                log.debug("Refund event sent - orderCode: {}, orderItemCode: {}, amount: {}",
                        order.getCode(), orderItem.getCode(), refundEvent.amount());
            }

            // 재고 복구 이벤트 발행 (Kafka 이벤트로 비동기 처리)
            try {
                orderEventProducer.sendInventoryRollback(orderItem.getProductCode(), orderItem.getQuantity());
                log.debug("재고 복구 이벤트 발행 완료 - productCode: {}, quantity: {}",
                        orderItem.getProductCode(), orderItem.getQuantity());
            } catch (Exception e) {
                String errorMessage = String.format("재고 복구 이벤트 발행 실패 - productCode: %s, quantity: %d, error: %s",
                        orderItem.getProductCode(), orderItem.getQuantity(), e.getMessage());
                log.error(errorMessage, e);
                // 보상 로직 실패 시 TransactionTracing에 실패 상태 저장
                compensationService.markCompensationFailed(order.getCode(), errorMessage);
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

    private UserResponse getUserInfo(String userCode) {
        return Optional.ofNullable(userServiceClient.getUserByCode(userCode))
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없습니다. userCode: {}", userCode);
                    return new BaseException(OrderErrorCode.USER_NOT_FOUND);
                });
    }

    private Order findOrderByCode(String orderCode) {
        return Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                .orElseThrow(() -> {
                    log.warn("주문을 찾을 수 없습니다. orderCode: {}", orderCode);
                    return new BaseException(OrderErrorCode.ORDER_NOT_FOUND);
                });
    }

}
