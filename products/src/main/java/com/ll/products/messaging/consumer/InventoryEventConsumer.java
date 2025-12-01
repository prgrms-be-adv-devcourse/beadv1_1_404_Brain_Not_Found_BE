package com.ll.products.messaging.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.InventoryEvent;
import com.ll.core.model.vo.kafka.enums.InventoryEventType;
import com.ll.products.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final ProductService productService;

    @KafkaListener(topics = "inventory-event", groupId = "product-service")
    public void handleInventoryEvent(KafkaEventEnvelope<InventoryEvent> event) {
        InventoryEvent inventoryEvent = event.payload();
        InventoryEventType eventType = inventoryEvent.eventType();
        String productCode = inventoryEvent.productCode();
        String referenceCode = inventoryEvent.referenceCode();
        int quantity = inventoryEvent.quantity();

        log.info("재고 이벤트 수신 - eventType: {}, productCode: {}, quantity: {}, referenceCode: {}", 
                eventType, productCode, quantity, referenceCode);

        try {
            if (eventType == InventoryEventType.STOCK_DECREMENT) {
                // 재고 차감: quantity를 음수로 전달
                productService.updateInventory(productCode, -quantity);
                log.info("재고 차감 완료 - productCode: {}, quantity: {}", productCode, quantity);
            } else if (eventType == InventoryEventType.STOCK_ROLLBACK) {
                // 재고 복구: quantity를 양수로 전달
                productService.updateInventory(productCode, quantity);
                log.info("재고 복구 완료 - productCode: {}, quantity: {}", productCode, quantity);
            } else {
                log.warn("알 수 없는 재고 이벤트 타입 - eventType: {}, productCode: {}", eventType, productCode);
            }
        } catch (Exception e) {
            log.error("재고 이벤트 처리 실패 - eventType: {}, productCode: {}, quantity: {}, error: {}", 
                    eventType, productCode, quantity, e.getMessage(), e);
            throw e; // 재시도를 위해 예외를 다시 던짐
        }
    }

    @KafkaListener(topics = "inventory-event.dlq", groupId = "product-service")
    public void handleInventoryDLQ(KafkaEventEnvelope<InventoryEvent> event) {
        InventoryEvent inventoryEvent = event.payload();
        log.error("재고 이벤트 DLQ 수신 - eventType: {}, productCode: {}, quantity: {}, referenceCode: {}. 수동 처리 필요", 
                inventoryEvent.eventType(), inventoryEvent.productCode(), 
                inventoryEvent.quantity(), inventoryEvent.referenceCode());
        // TODO: DLQ 처리 로직 추가 (알림, 모니터링 등)
    }
}

