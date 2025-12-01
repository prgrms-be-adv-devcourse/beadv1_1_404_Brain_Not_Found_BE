package com.ll.products.domain.product.event;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.InventoryEvent;
import com.ll.core.model.vo.kafka.enums.InventoryEventType;
import com.ll.products.domain.product.model.entity.DlqStatus;
import com.ll.products.domain.product.model.entity.InventoryDlqEvent;
import com.ll.products.domain.product.repository.InventoryDlqEventRepository;
import com.ll.products.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final ProductService productService;
    private final InventoryDlqEventRepository inventoryDlqEventRepository;

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
    @Transactional
    public void handleInventoryDLQ(KafkaEventEnvelope<InventoryEvent> event) {
        InventoryEvent inventoryEvent = event.payload();
        
        log.error("재고 이벤트 DLQ 수신 - eventType: {}, productCode: {}, quantity: {}, referenceCode: {}. 수동 처리 필요", 
                inventoryEvent.eventType(), inventoryEvent.productCode(), 
                inventoryEvent.quantity(), inventoryEvent.referenceCode());
        
        // DLQ 이벤트 저장 (수동 처리 가능하도록)
        InventoryDlqEvent dlqEvent = InventoryDlqEvent.builder()
                .productCode(inventoryEvent.productCode())
                .quantity(inventoryEvent.quantity())
                .eventType(inventoryEvent.eventType())
                .referenceCode(inventoryEvent.referenceCode())
                .retryCount(5) // 이미 5번 재시도했음
                .status(DlqStatus.PENDING) // 수동 처리 대기
                .failedAt(LocalDateTime.now())
                .build();
        
        inventoryDlqEventRepository.save(dlqEvent);
        
        log.info("DLQ 이벤트 저장 완료 - id: {}, productCode: {}, referenceCode: {}", 
                dlqEvent.getId(), dlqEvent.getProductCode(), dlqEvent.getReferenceCode());
        
        // TODO: 알림 발송 (Slack, Email 등)
        // TODO: 모니터링 메트릭 전송
    }
}

