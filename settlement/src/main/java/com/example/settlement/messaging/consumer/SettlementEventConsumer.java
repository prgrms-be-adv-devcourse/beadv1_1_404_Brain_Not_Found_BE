package com.example.settlement.messaging.consumer;

import com.example.core.model.vo.kafka.SettlementEvent;
import com.example.core.model.vo.kafka.OrderEvent;
import com.example.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventConsumer {

    private final SettlementService settlementService;

    @KafkaListener(topics = "order-event", groupId = "settlement-service")
    public void handleOrderCompleteEvent(OrderEvent event) {
        try {
            log.info("Received order complete event from settlement service : {}", event.toString());
            settlementService.createSettlement(event);
        } catch (Exception e) {
            log.error("Failed to process OrderCompleteEvent for OrderItemCode {}: {}", event.orderItemCode(), e.getMessage());
            // TODO: 보상 처리 로직 추가 ( Dead Letter Queue 는 KafkaConfig 에서 설정 완료 )
        }
    }

    @KafkaListener(topics = "settlement-event.dlq", groupId = "settlement-service.dlq")
    public void handleDLQ(SettlementEvent event) {
        log.error("Received message in DLQ for OrderItemCode {}: {}", event.orderItemCode(), event);
        settlementService.failByDlqEvent(event);
    }
}
