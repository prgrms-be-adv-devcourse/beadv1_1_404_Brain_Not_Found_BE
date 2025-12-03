package com.ll.order.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.exception.BaseException;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.order.domain.client.*;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.entity.TransactionTracing;
import com.ll.order.domain.model.entity.history.OrderHistoryBuilder;
import com.ll.order.domain.model.entity.history.OrderHistoryEntity;
import com.ll.order.domain.model.entity.OrderEventOutbox;
import com.ll.order.domain.model.enums.order.OrderHistoryActionType;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.transaction.CompensationStatus;
import com.ll.order.domain.model.enums.payment.PaidType;
import com.ll.order.domain.model.vo.InventoryDeduction;
import com.ll.order.domain.model.vo.response.cart.CartItemsResponse;
import com.ll.order.domain.model.vo.response.order.OrderCreateResponse;
import com.ll.order.domain.model.vo.response.order.OrderCreationResult;
import com.ll.order.domain.model.vo.response.product.ProductResponse;
import com.ll.order.domain.model.vo.response.user.UserResponse;
import com.ll.order.domain.repository.OrderEventOutboxRepository;
import com.ll.order.domain.repository.OrderHistoryJpaRepository;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.domain.repository.TransactionTracingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOrderCreationService {

    // 공통 의존성 (protected로 선언하여 하위 클래스에서 접근 가능)
    protected final OrderJpaRepository orderJpaRepository;
    protected final OrderItemJpaRepository orderItemJpaRepository;
    protected final OrderHistoryJpaRepository orderHistoryJpaRepository;
    protected final OrderEventOutboxRepository orderEventOutboxRepository;
    protected final TransactionTracingRepository transactionTracingRepository;

    protected final UserServiceClient userServiceClient;
    protected final ProductServiceClient productServiceClient;
    protected final CartServiceClient cartServiceClient;
    protected final PaymentServiceClient paymentApiClient;

    protected final OrderEventProducer orderEventProducer;
    protected final ObjectMapper objectMapper;
    protected final OrderValidator orderValidator;

    public final OrderCreateResponse createOrder(Object request, String userCode) {
        UserResponse userInfo = getUserInfo(userCode);

        // 1. 주문 생성 전 재고 가용성 체크 (읽기만, 락 없음)
        validateInventory(request);

        // 2. 주문 및 주문 상품 생성 (독립 트랜잭션 - 항상 커밋)
        OrderCreationResult creationResult = createOrderWithItems(request, userInfo);
        Order savedOrder = creationResult.order();
        List<OrderItem> orderItems = creationResult.orderItems();

        // 3. 재고 차감 (주문 생성 후, 결제 전)
        updateProductInventory(savedOrder, orderItems, userInfo.code());

        // 4. 결제 처리 (별도 트랜잭션)
        PaidType paidType = extractPaidType(request);
        if (paidType == PaidType.DEPOSIT) {
            processDepositPayment(savedOrder, orderItems, request);
        } else if (paidType == PaidType.TOSS_PAYMENT) {
            // 토스 결제: 주문만 생성하고 결제는 UI에서 처리
            // 주문 상태는 CREATED로 유지 (결제 완료 후 completePaymentWithKey에서 COMPLETED로 변경)
            log.debug("토스 결제 주문 생성 완료 - orderId: {}, orderCode: {}, 상태: CREATED (결제 대기)",
                    savedOrder.getId(), savedOrder.getCode());
        } else {
            log.warn("지원하지 않는 결제 수단입니다. paidType: {}", paidType);
            throw new BaseException(OrderErrorCode.UNSUPPORTED_PAYMENT_TYPE);
        }

        return convertToOrderCreateResponse(savedOrder);
    }

    // ========== 추상 메서드들 (하위 클래스에서 구현해야 함) ==========

    protected abstract void validateInventory(Object request);

    @Transactional
    protected abstract OrderCreationResult createOrderWithItems(Object request, UserResponse userInfo);

    @Transactional
    protected abstract void processDepositPayment(Order order, List<OrderItem> orderItems, Object request);

    protected abstract PaidType extractPaidType(Object request);

    protected abstract String extractPaymentKey(Object request);

    // ========== 공통 메서드들 (하위 클래스에서 사용 가능) ==========
    protected UserResponse getUserInfo(String userCode) {
        return Optional.ofNullable(userServiceClient.getUserByCode(userCode))
                .orElseThrow(() -> {
                    log.warn("사용자를 찾을 수 없습니다. userCode: {}", userCode);
                    return new BaseException(OrderErrorCode.USER_NOT_FOUND);
                });
    }

    protected ProductResponse getProductInfo(String productCode) {
        return Optional.ofNullable(productServiceClient.getProductByCode(productCode))
                .orElseThrow(() -> {
                    log.warn("상품을 찾을 수 없습니다. productCode: {}", productCode);
                    return new BaseException(OrderErrorCode.PRODUCT_NOT_FOUND);
                });
    }

    protected CartItemsResponse getCartInfo(String cartCode) {
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

    protected Order findOrderByCode(String orderCode) {
        return Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                .orElseThrow(() -> {
                    log.warn("주문을 찾을 수 없습니다. orderCode: {}", orderCode);
                    return new BaseException(OrderErrorCode.ORDER_NOT_FOUND);
                });
    }

    protected OrderCreateResponse convertToOrderCreateResponse(Order order) {
        List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());
        return OrderCreateResponse.from(order, orderItems);
    }

    protected void updateProductInventory(Order order, List<OrderItem> orderItems, String buyerCode) {
        List<String> failedProducts = new ArrayList<>();
        List<InventoryDeduction> successfulDeductions = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            // 재고 감소 (동기 API 호출) <- 비관적 락 적용 시점
            try {
                productServiceClient.decreaseInventory(orderItem.getProductCode(), orderItem.getQuantity());
                log.debug("재고 차감 완료 - productCode: {}, quantity: {}",
                        orderItem.getProductCode(), orderItem.getQuantity());
                // 성공한 재고 차감 정보 저장 (롤백용)
                successfulDeductions.add(new InventoryDeduction(
                        orderItem.getProductCode(),
                        orderItem.getQuantity()
                ));
            } catch (Exception e) {
                log.error("재고 차감 실패 - productCode: {}, quantity: {}, error: {}",
                        orderItem.getProductCode(), orderItem.getQuantity(), e.getMessage(), e);
                failedProducts.add(orderItem.getProductCode());
            }
        }

        // 재고 차감 실패 시 성공한 재고 차감 롤백
        if (!failedProducts.isEmpty()) {
            log.error("재고 차감 실패 - orderCode: {}, failedProducts: {}", order.getCode(), failedProducts);

            // 성공한 재고 차감 롤백
            if (!successfulDeductions.isEmpty()) {
                rollbackInventory(successfulDeductions, order.getCode());
            }

            throw new BaseException(OrderErrorCode.INVENTORY_DEDUCTION_FAILED);
        }
    }

    protected void rollbackInventory(List<InventoryDeduction> successfulDeductions, String orderCode) {
        log.warn("재고 차감 실패로 인한 재고 롤백 시작 - 롤백 대상: {}개", successfulDeductions.size());

        boolean hasFailure = false;
        String lastErrorMessage = null;

        for (InventoryDeduction deduction : successfulDeductions) {
            try {
                orderEventProducer.sendInventoryRollback(deduction.productCode(), deduction.quantity());
                log.debug("재고 롤백 이벤트 발행 완료 - productCode: {}, quantity: {}",
                        deduction.productCode(), deduction.quantity());
            } catch (Exception e) {
                hasFailure = true;
                lastErrorMessage = String.format("재고 롤백 이벤트 발행 실패 - productCode: %s, quantity: %d, error: %s",
                        deduction.productCode(), deduction.quantity(), e.getMessage());
                log.error(lastErrorMessage, e);
            }
        }

        // 보상 로직 실패 시 TransactionTracing에 실패 상태 저장
        if (hasFailure && orderCode != null) {
            markCompensationFailed(orderCode, lastErrorMessage);
        }
    }

    public void rollbackInventoryForOrder(List<OrderItem> orderItems, String orderCode) {
        log.warn("결제 실패로 인한 재고 롤백 시작 - orderItems: {}개", orderItems.size());

        List<InventoryDeduction> deductions = orderItems.stream()
                .map(item -> new InventoryDeduction(item.getProductCode(), item.getQuantity()))
                .toList();

        rollbackInventory(deductions, orderCode);
    }

    protected void publishOrderCompletedEvents(Order order, List<OrderItem> orderItems, String buyerCode) {
        for (OrderItem orderItem : orderItems) {
            OrderEvent orderEvent = OrderEvent.of(
                    buyerCode,
                    orderItem.getSellerCode(),
                    orderItem.getCode(),
                    order.getCode(),
                    (long) orderItem.getPrice() * orderItem.getQuantity()
            );

            // Outbox 패턴: 트랜잭션 내에서 먼저 Outbox에 저장 (PENDING 상태)
            // 별도 프로세스가 Outbox를 읽어서 Kafka에 발행
            saveToOutbox(orderEvent, order.getCode(), orderItem.getCode());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToOutbox(OrderEvent orderEvent, String orderCode, String orderItemCode) {
        try {
            OrderEventOutbox outbox = OrderEventOutbox.from(orderEvent, objectMapper);
            // PENDING 상태로 저장 (기본값이므로 명시적으로 설정하지 않아도 됨)
            orderEventOutboxRepository.save(outbox);

            log.debug("주문 이벤트 Outbox 저장 완료 (PENDING) - orderCode: {}, orderItemCode: {}, referenceCode: {}, outboxId: {}",
                    orderCode, orderItemCode, orderEvent.referenceCode(), outbox.getId());
        } catch (Exception e) {
            log.error("주문 이벤트 Outbox 저장 실패 - orderCode: {}, orderItemCode: {}, referenceCode: {}, error: {}",
                    orderCode, orderItemCode, orderEvent.referenceCode(), e.getMessage(), e);
            // Outbox 저장 실패는 로그만 남기고 계속 진행 (수동 처리 필요)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompensationFailed(String orderCode, String errorMessage) {
        try {
            TransactionTracing tracing = transactionTracingRepository.findByOrderCode(orderCode)
                    .orElse(null);

            if (tracing != null) {
                // 보상 시작 상태로 변경 (아직 시작하지 않았다면)
                if (tracing.getCompensationStatus() == CompensationStatus.NONE) {
                    tracing.startCompensation();
                }
                // 보상 실패 상태로 변경
                tracing.markCompensationFailed(errorMessage);

                log.debug("보상 로직 실패 상태 저장 완료 - orderCode: {}, retryCount: {}",
                        orderCode, tracing.getCompensationRetryCount());
            } else {
                log.debug("TransactionTracing을 찾을 수 없습니다. orderCode: {}", orderCode);
            }
        } catch (Exception e) {
            log.error("보상 로직 실패 상태 저장 실패 - orderCode: {}, error: {}",
                    orderCode, e.getMessage(), e);
        }
    }

    protected void saveOrderHistory(Order order, List<OrderItem> orderItems, OrderHistoryActionType actionType,
                                   OrderStatus previousStatus, String reason, String errorMessage, String createdBy) {
        OrderHistoryEntity orderHistory = OrderHistoryBuilder.builder()
                .order(order)
                .orderItems(orderItems)
                .actionType(actionType)
                .previousStatus(previousStatus)
                .reason(reason)
                .errorMessage(errorMessage)
                .createdBy(createdBy)
                .build();
        orderHistoryJpaRepository.save(orderHistory);
    }
}

