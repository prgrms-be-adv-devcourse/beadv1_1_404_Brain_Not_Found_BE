package com.ll.order.domain.service.order.create.strategy;

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
import com.ll.order.domain.model.vo.request.OrderCartItemRequest;
import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import com.ll.order.domain.model.vo.response.cart.CartItemInfo;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import com.ll.order.domain.model.vo.response.order.OrderCreationResult;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderEventOutboxRepository;
import com.ll.order.domain.repository.OrderHistoryJpaRepository;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.domain.service.order.create.AbstractOrderCreationService;
import com.ll.order.domain.service.compensation.CompensationService;
import com.ll.order.domain.service.event.OrderEventOutboxService;
import com.ll.order.domain.service.order.OrderValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CartOrderCreationStrategy extends AbstractOrderCreationService {

    public CartOrderCreationStrategy(
            OrderJpaRepository orderJpaRepository,
            OrderItemJpaRepository orderItemJpaRepository,
            OrderHistoryJpaRepository orderHistoryJpaRepository,
            OrderEventOutboxRepository orderEventOutboxRepository,
            UserServiceClient userServiceClient,
            ProductServiceClient productServiceClient,
            CartServiceClient cartServiceClient,
            PaymentServiceClient paymentApiClient,
            OrderEventProducer orderEventProducer,
            ObjectMapper objectMapper,
            OrderValidator orderValidator,
            OrderEventOutboxService orderEventOutboxService,
            CompensationService compensationService
    ) {
        super(orderJpaRepository, orderItemJpaRepository, orderHistoryJpaRepository,
                orderEventOutboxRepository,
                userServiceClient, productServiceClient, cartServiceClient,
                paymentApiClient, orderEventProducer, objectMapper, orderValidator,
                orderEventOutboxService, compensationService);
    }

    @Override
    protected void validateInventory(Object request) {
        if (!(request instanceof OrderCartItemRequest cartRequest)) {
            throw new IllegalArgumentException("OrderCartItemRequest 타입이 아닙니다.");
        }

        CartItemsResponse cartInfo = getCartInfo(cartRequest.cartCode());

        for (CartItemInfo item : cartInfo.items()) {
            // 재고 및 판매 상태 검증 (OrderValidator의 공통 메서드 사용)
            orderValidator.validateProductInventory(item.productCode(), item.quantity());
        }
    }

    //     주문 및 주문 상품 생성 (독립 트랜잭션)
    //     결제 실패와 무관하게 주문은 항상 저장됨
    @Override
    @Transactional
    protected OrderCreationResult createOrderWithItems(Object request, UserResponse userInfo) {
        if (!(request instanceof OrderCartItemRequest cartRequest)) {
            throw new IllegalArgumentException("OrderCartItemRequest 타입이 아닙니다.");
        }

        CartItemsResponse cartInfo = getCartInfo(cartRequest.cartCode());

        List<ProductResponse> productList = new ArrayList<>();
        for (CartItemInfo item : cartInfo.items()) {
            ProductResponse productInfo = getProductInfo(item.productCode());
            productList.add(productInfo);
        }

        Order order = Order.create(
                userInfo.id(),
                userInfo.code(),
                cartRequest.orderType(),
                cartRequest.address()
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

        // 주문 생성 이력 저장
        OrderHistoryEntity orderHistory = OrderHistoryBuilder.createOrderHistory(savedOrder, orderItems);
        orderHistoryJpaRepository.save(orderHistory);

        return new OrderCreationResult(savedOrder, orderItems);
    }

    @Override
    @Transactional
    protected void processDepositPayment(Order order, List<OrderItem> orderItems, Object request) {
        if (!(request instanceof OrderCartItemRequest cartRequest)) {
            throw new IllegalArgumentException("OrderCartItemRequest 타입이 아닙니다.");
        }

        OrderPaymentRequest orderPaymentRequest = OrderPaymentRequest.from(
                order,
                order.getBuyerCode(),
                cartRequest.paidType(),
                cartRequest.paymentKey()
        );

        try {
            paymentApiClient.requestDepositPayment(orderPaymentRequest);

            order.changeStatus(OrderStatus.COMPLETED);
            orderJpaRepository.save(order);

            // 주문 상태 변경 이력 저장 (결제 성공)
            OrderHistoryEntity successHistory = OrderHistoryBuilder.createPaymentSuccessHistory(
                    order, orderItems, order.getOrderStatus(), "예치금");
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
                    order, orderItems, order.getOrderStatus(), "예치금", e.getMessage());
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
        if (!(request instanceof OrderCartItemRequest cartRequest)) {
            throw new IllegalArgumentException("OrderCartItemRequest 타입이 아닙니다.");
        }
        return cartRequest.paidType();
    }

    @Override
    protected String extractPaymentKey(Object request) {
        if (!(request instanceof OrderCartItemRequest cartRequest)) {
            throw new IllegalArgumentException("OrderCartItemRequest 타입이 아닙니다.");
        }
        return cartRequest.paymentKey();
    }
}

