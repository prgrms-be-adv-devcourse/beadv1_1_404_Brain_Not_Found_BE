package com.ll.settlement.messaging.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.core.model.vo.kafka.SettlementEvent;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventConsumer {

    private final SettlementService settlementService;

    @KafkaListener(topics = "settlement-event.dlq", groupId = "settlement-service")
    public void handleSettlementDLQ(KafkaEventEnvelope<SettlementEvent> event) {
        settlementService.failByDlqEvent(event.payload());
    }

    @KafkaListener(topics = "order-event", groupId = "settlement-service")
    public void handleOrderEvent(KafkaEventEnvelope<OrderEvent> event) {
        if ( !event.payload().orderEventType().toString().equals("ORDER_COMPLETED") ) {
            return;
        }
        settlementService.createSettlement(event.payload());
    }

    @KafkaListener(topics = "order-event.dlq", groupId = "settlement-service")
    public void handleOrderDLQ(KafkaEventEnvelope<OrderEvent> event) {
        if ( !event.payload().orderEventType().toString().equals("ORDER_COMPLETED") ) {
            return;
        }
    }

    @KafkaListener(topics = "refund-event", groupId = "settlement-service")
    public void handleRefundEvent(KafkaEventEnvelope<RefundEvent> event) {
        settlementService.refundSettlement(event.payload());
    }

    @KafkaListener(topics = "refund-event.dlq", groupId = "settlement-service")
    public void handleRefundDLQ(KafkaEventEnvelope<RefundEvent> event) {
    }

}
