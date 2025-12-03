package com.ll.order.domain.service.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.client.CartServiceClient;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.client.ProductServiceClient;
import com.ll.order.domain.client.UserServiceClient;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.entity.history.OrderHistoryBuilder;
import com.ll.order.domain.model.entity.history.OrderHistoryEntity;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.vo.request.OrderDirectRequest;
import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import com.ll.order.domain.model.vo.response.order.OrderCreationResult;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderEventOutboxRepository;
import com.ll.order.domain.repository.OrderHistoryJpaRepository;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.domain.repository.TransactionTracingRepository;
import com.ll.order.domain.service.AbstractOrderCreationService;
import com.ll.order.domain.service.OrderValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class DirectOrderCreationStrategy extends AbstractOrderCreationService {

    public DirectOrderCreationStrategy(
            OrderJpaRepository orderJpaRepository,
            OrderItemJpaRepository orderItemJpaRepository,
            OrderHistoryJpaRepository orderHistoryJpaRepository,
            OrderEventOutboxRepository orderEventOutboxRepository,
            TransactionTracingRepository transactionTracingRepository,
            UserServiceClient userServiceClient,
            ProductServiceClient productServiceClient,
            CartServiceClient cartServiceClient,
            PaymentServiceClient paymentApiClient,
            OrderEventProducer orderEventProducer,
            ObjectMapper objectMapper,
            OrderValidator orderValidator
    ) {
        super(orderJpaRepository, orderItemJpaRepository, orderHistoryJpaRepository,
                orderEventOutboxRepository, transactionTracingRepository,
                userServiceClient, productServiceClient, cartServiceClient,
                paymentApiClient, orderEventProducer, objectMapper, orderValidator);
    }

    @Override
    protected void validateInventory(Object request) {
        if (!(request instanceof OrderDirectRequest directRequest)) {
            throw new IllegalArgumentException("OrderDirectRequest 타입이 아닙니다.");
        }

        // 재고 및 판매 상태 검증 (OrderValidator의 공통 메서드 사용)
        orderValidator.validateProductInventory(directRequest.productCode(), directRequest.quantity());
    }

    @Override
    @Transactional
    protected OrderCreationResult createOrderWithItems(Object request, UserResponse userInfo) {
        if (!(request instanceof OrderDirectRequest directRequest)) {
            throw new IllegalArgumentException("OrderDirectRequest 타입이 아닙니다.");
        }

        ProductResponse productInfo = getProductInfo(directRequest.productCode());

        log.debug("상품 정보 조회 완료 - id: {}, code: {}, name: {}, sellerCode: {}, sellerName: {}, quantity: {}, price: {}, status: {}",
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
                directRequest.orderType(),
                directRequest.address()
        );
        Order savedOrder = orderJpaRepository.save(order);

        OrderItem orderItem = savedOrder.createOrderItem(
                productInfo.id(),
                productInfo.code(),
                productInfo.sellerCode(),
                productInfo.name(),
                directRequest.quantity(),
                productInfo.price()
        );
        orderItemJpaRepository.save(orderItem);

        // 주문 생성 이력 저장
        OrderHistoryEntity orderHistory = OrderHistoryBuilder.createOrderHistory(savedOrder, List.of(orderItem));
        orderHistoryJpaRepository.save(orderHistory);

        return new OrderCreationResult(savedOrder, List.of(orderItem));
    }

    @Override
    @Transactional
    protected void processDepositPayment(Order order, List<OrderItem> orderItems, Object request) {
        if (!(request instanceof OrderDirectRequest directRequest)) {
            throw new IllegalArgumentException("OrderDirectRequest 타입이 아닙니다.");
        }

        OrderPaymentRequest orderPaymentRequest = OrderPaymentRequest.from(
                order,
                order.getBuyerCode(),
                directRequest.paidType(),
                directRequest.paymentKey()
        );

        OrderStatus previousStatus = order.getOrderStatus();

        try {
            paymentApiClient.requestDepositPayment(orderPaymentRequest);

            order.changeStatus(OrderStatus.COMPLETED);
            orderJpaRepository.save(order);

            // 주문 상태 변경 이력 저장 (결제 성공)
            OrderHistoryEntity successHistory = OrderHistoryBuilder.createPaymentSuccessHistory(
                    order, orderItems, previousStatus, "예치금");
            orderHistoryJpaRepository.save(successHistory);

            // 주문 완료 이벤트 발행 (주문 상태가 COMPLETED일 때)
            publishOrderCompletedEvents(order, orderItems, order.getBuyerCode());

            log.debug("예치금 결제 완료 - orderCode: {}, amount: {}",
                    order.getCode(), order.getTotalPrice());
        } catch (Exception e) {
            order.changeStatus(OrderStatus.FAILED);
            orderJpaRepository.save(order);

            // 주문 상태 변경 이력 저장 (결제 실패)
            OrderHistoryEntity failHistory = OrderHistoryBuilder.createPaymentFailHistory(
                    order, orderItems, previousStatus, "예치금", e.getMessage());
            orderHistoryJpaRepository.save(failHistory);

            // 결제 실패 시 재고 롤백 (재고 차감이 결제 전에 이루어졌기 때문)
            rollbackInventoryForOrder(orderItems, order.getCode());

            log.error("결제 처리 실패 - orderCode: {}, error: {}",
                    order.getCode(), e.getMessage(), e);

            throw new BaseException(OrderErrorCode.PAYMENT_PROCESSING_FAILED);
        }
    }

    @Override
    protected PaidType extractPaidType(Object request) {
        if (!(request instanceof OrderDirectRequest directRequest)) {
            throw new IllegalArgumentException("OrderDirectRequest 타입이 아닙니다.");
        }
        return directRequest.paidType();
    }

    @Override
    protected String extractPaymentKey(Object request) {
        if (!(request instanceof OrderDirectRequest directRequest)) {
            throw new IllegalArgumentException("OrderDirectRequest 타입이 아닙니다.");
        }
        return directRequest.paymentKey();
    }
}

