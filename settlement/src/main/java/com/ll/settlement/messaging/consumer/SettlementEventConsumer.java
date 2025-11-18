package com.ll.settlement.messaging.consumer;

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
    public void handleSettlementDLQ(SettlementEvent event) {
        log.error("[Settlement][Settlement Module] Received message in DLQ for OrderItemCode {}", event);
        settlementService.failByDlqEvent(event);
    }

    @KafkaListener(topics = "order-event", groupId = "settlement-service")
    public void handleOrderEvent(OrderEvent event) {
        if ( !event.orderEventType().toString().equals("ORDER_COMPLETED") ) {
            return;
        }
        log.info("[Order][Settlement Module] Received order complete event from Order service : {}", event);
        settlementService.createSettlement(event);
    }

    @KafkaListener(topics = "order-event.dlq", groupId = "settlement-service")
    public void handleOrderDLQ(OrderEvent event) {
        if ( !event.orderEventType().toString().equals("ORDER_COMPLETED") ) {
            return;
        }
        log.error("[Order][Settlement Module] Received message in DLQ for OrderItemCode {}", event);
    }

    @KafkaListener(topics = "refund-event", groupId = "settlement-service")
    public void handleRefundEvent(RefundEvent event) {
        log.info("[Refund][Settlement Module] Received refund complete event from Order service : {}", event);
        settlementService.setSettlementStatusToRefunded(event);
    }

    @KafkaListener(topics = "refund-event.dlq", groupId = "settlement-service")
    public void handleRefundDLQ(RefundEvent event) {
        log.error("[Refund][Settlement Module] Received message in DLQ for OrderItemCode {}", event);
    }

}
