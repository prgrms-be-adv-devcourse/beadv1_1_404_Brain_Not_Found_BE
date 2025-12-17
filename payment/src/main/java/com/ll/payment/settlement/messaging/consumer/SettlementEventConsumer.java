package com.ll.payment.settlement.messaging.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.payment.settlement.service.SettlementService;
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
    public void handleOrderEvent(KafkaEventEnvelope<OrderEvent> event) {
        settlementService.createSettlement(event.payload());
    }

    @KafkaListener(topics = "order-event.dlq", groupId = "settlement-service")
    public void handleOrderDLQ(KafkaEventEnvelope<OrderEvent> event) {
    }

}
