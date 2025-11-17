package com.ll.products.domain.product.messaging.consumer;

import com.ll.core.model.vo.kafka.InventoryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    @KafkaListener(topics = "inventory-event", groupId = "product-service")
    public void handleInventoryEvent(InventoryEvent event) {
        if ( event.eventType().toString().equals("STOCK_DECREMENT") ) {
            log.info("[Inventory][Product Module] Received Inventory Decrement Event from Order service : {}", event);
            // 재고 차감 로직 처리
        } else if ( event.eventType().toString().equals("STOCK_ROLLBACK") ) {
            log.info("[Inventory][Product Module] Received Inventory Rollback Event from Order service : {}", event);
            // 재고 롤백 로직 처리
        }
    }

    @KafkaListener(topics = "inventory-event.dlq", groupId = "product-service")
    public void handleInventoryDLQ(InventoryEvent event) {
        log.error("[Inventory][Product Module][DLQ] Received message in DLQ: {}", event);
        // DLQ 처리 로직 (예: 알림, 재처리 시도 등)
    }
}
