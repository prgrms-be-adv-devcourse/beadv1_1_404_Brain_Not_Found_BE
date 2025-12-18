package com.ll.order.domain.service.inventory;

import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.vo.InventoryDeduction;
import com.ll.order.domain.service.compensation.CompensationService;
import com.ll.order.domain.service.event.InventoryRollbackEventOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInventoryService {

    private final CompensationService compensationService;
    private final InventoryRollbackEventOutboxService inventoryRollbackEventOutboxService;

    public void rollbackInventoryForOrder(List<OrderItem> orderItems, String orderCode) {
        log.warn("결제 실패로 인한 재고 롤백 시작 - orderItems: {}개", orderItems.size());

        List<InventoryDeduction> deductions = orderItems.stream()
                .map(item -> new InventoryDeduction(item.getProductCode(), item.getQuantity()))
                .toList();

        rollbackInventory(deductions, orderCode);
    }

    public void rollbackInventory(List<InventoryDeduction> successfulDeductions, String orderCode) {
        log.warn("재고 차감 실패로 인한 재고 롤백 시작 - 롤백 대상: {}개", successfulDeductions.size());

        boolean hasFailure = false;
        String lastErrorMessage = null;

        for (InventoryDeduction deduction : successfulDeductions) {
            try {
                // Outbox 패턴: 트랜잭션 내에서 먼저 Outbox에 저장 (PENDING 상태)
                // 별도 프로세스가 Outbox를 읽어서 Kafka에 발행
                inventoryRollbackEventOutboxService.saveToOutbox(
                        orderCode,
                        deduction.productCode(),
                        deduction.quantity()
                );
                log.debug("재고 롤백 이벤트 Outbox 저장 완료 - orderCode: {}, productCode: {}, quantity: {}",
                        orderCode, deduction.productCode(), deduction.quantity());
            } catch (Exception e) {
                hasFailure = true;
                lastErrorMessage = String.format("재고 롤백 이벤트 Outbox 저장 실패 - orderCode: %s, productCode: %s, quantity: %d, error: %s",
                        orderCode, deduction.productCode(), deduction.quantity(), e.getMessage());
                log.error(lastErrorMessage, e);
            }
        }

        // 보상 로직 실패 시 TransactionTracing에 실패 상태 저장
        if (hasFailure && orderCode != null) {
            compensationService.compensationFailed(orderCode, lastErrorMessage);
        }
    }
}

