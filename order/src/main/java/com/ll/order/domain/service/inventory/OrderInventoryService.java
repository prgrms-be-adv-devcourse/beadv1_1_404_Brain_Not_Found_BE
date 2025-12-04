package com.ll.order.domain.service.inventory;

import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.vo.InventoryDeduction;
import com.ll.order.domain.service.compensation.CompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInventoryService {

    private final OrderEventProducer orderEventProducer;
    private final CompensationService compensationService;

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
            compensationService.markCompensationFailed(orderCode, lastErrorMessage);
        }
    }
}

