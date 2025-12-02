package com.ll.order.domain.service;

import com.ll.core.model.exception.BaseException;
import com.ll.order.domain.client.PaymentServiceClient;
import com.ll.order.domain.exception.OrderErrorCode;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.entity.TransactionTracing;
import com.ll.order.domain.model.entity.TransactionTracing.CompensationStatus;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.repository.OrderItemJpaRepository;
import com.ll.order.domain.repository.OrderJpaRepository;
import com.ll.order.domain.repository.TransactionTracingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompensationRetryService {

    private final TransactionTracingRepository transactionTracingRepository;
    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    private final OrderEventProducer orderEventProducer;
    private final PaymentServiceClient paymentServiceClient;

    @Value("${order.compensation.max-retry-count:5}")
    private Integer maxRetryCount;

//     실패한 보상 로직을 재시도합니다.
    @Transactional
    public int retryFailedCompensations() {
        // 재시도 대상 조회: 보상 상태가 FAILED이고, 재시도 횟수가 최대값 미만인 것
        List<TransactionTracing> failedCompensations = transactionTracingRepository
                .findFailedCompensationsForRetry(CompensationStatus.FAILED, maxRetryCount);

        if (failedCompensations.isEmpty()) {
            log.debug("재시도할 실패한 보상 로직이 없습니다.");
            return 0;
        }

        log.debug("실패한 보상 로직 재시도 시작 - 대상: {}개", failedCompensations.size());

        int successCount = 0;
        int failureCount = 0;

        for (TransactionTracing tracing : failedCompensations) {
            try {
                retryCompensation(tracing);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("보상 로직 재시도 실패 - orderCode: {}, error: {}",
                        tracing.getOrderCode(), e.getMessage(), e);
            }
        }

        log.debug("보상 로직 재시도 완료 - 성공: {}개, 실패: {}개", successCount, failureCount);
        return successCount;
    }

    @Transactional
    public void retryCompensation(TransactionTracing tracing) {
        String orderCode = tracing.getOrderCode();
        
        // 보상 시작 상태로 변경
        tracing.startCompensation();
        transactionTracingRepository.save(tracing);

        try {
            // Order 조회
            Order order = Optional.ofNullable(orderJpaRepository.findByCode(orderCode))
                    .orElseThrow(() -> {
                        log.warn("주문을 찾을 수 없습니다. orderCode: {}", orderCode);
                        return new BaseException(OrderErrorCode.ORDER_NOT_FOUND);
                    });

            List<OrderItem> orderItems = orderItemJpaRepository.findByOrderId(order.getId());

            // 보상 로직 실행
            boolean compensationSuccess = executeCompensation(order, orderItems, tracing);

            if (compensationSuccess) {
                // 보상 완료 상태로 변경
                tracing.markCompensationCompleted();
                transactionTracingRepository.save(tracing);
                
                log.debug("보상 로직 재시도 성공 - orderCode: {}, retryCount: {}",
                        orderCode, tracing.getCompensationRetryCount());
            } else {
                // 보상 실패 상태로 변경 (재시도 횟수 증가)
                tracing.markCompensationFailed("보상 로직 실행 중 일부 실패");
                transactionTracingRepository.save(tracing);
                
                log.warn("보상 로직 재시도 부분 실패 - orderCode: {}, retryCount: {}",
                        orderCode, tracing.getCompensationRetryCount());
                throw new RuntimeException("보상 로직 실행 중 일부 실패");
            }

        } catch (Exception e) {
            // 보상 실패 상태로 변경 (재시도 횟수 증가)
            tracing.markCompensationFailed(e.getMessage());
            transactionTracingRepository.save(tracing);
            
            log.error("보상 로직 재시도 실패 - orderCode: {}, retryCount: {}, error: {}",
                    orderCode, tracing.getCompensationRetryCount(), e.getMessage(), e);
            throw e;
        }
    }

    private boolean executeCompensation(Order order, List<OrderItem> orderItems, TransactionTracing tracing) {
        boolean allSuccess = true;

        // 1. 재고 롤백 (항상 필요)
        boolean inventoryRollbackSuccess = rollbackInventory(orderItems);
        if (!inventoryRollbackSuccess) {
            allSuccess = false;
        }

        // 2. 결제 환불 (주문 상태가 COMPLETED인 경우만 필요)
        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            boolean refundSuccess = refundPayment(order);
            if (!refundSuccess) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    private boolean rollbackInventory(List<OrderItem> orderItems) {
        boolean allSuccess = true;

        for (OrderItem orderItem : orderItems) {
            try {
                orderEventProducer.sendInventoryRollback(
                        orderItem.getProductCode(),
                        orderItem.getQuantity()
                );
                log.debug("재고 롤백 이벤트 재발행 성공 - productCode: {}, quantity: {}",
                        orderItem.getProductCode(), orderItem.getQuantity());
            } catch (Exception e) {
                allSuccess = false;
                log.error("재고 롤백 이벤트 재발행 실패 - productCode: {}, quantity: {}, error: {}",
                        orderItem.getProductCode(), orderItem.getQuantity(), e.getMessage(), e);
            }
        }

        return allSuccess;
    }

    private boolean refundPayment(Order order) {
        try {
            paymentServiceClient.requestRefund(
                    order.getId(),
                    order.getCode(),
                    order.getBuyerCode(),
                    order.getTotalPrice(),
                    "보상 로직 재시도 - 주문 취소"
            );
            log.debug("환불 처리 재시도 성공 - orderCode: {}, amount: {}",
                    order.getCode(), order.getTotalPrice());
            return true;
        } catch (Exception e) {
            log.error("환불 처리 재시도 실패 - orderCode: {}, amount: {}, error: {}",
                    order.getCode(), order.getTotalPrice(), e.getMessage(), e);
            return false;
        }
    }

    public long countRetryableCompensations() {
        return transactionTracingRepository
                .findFailedCompensationsForRetry(CompensationStatus.FAILED, maxRetryCount)
                .size();
    }
}

