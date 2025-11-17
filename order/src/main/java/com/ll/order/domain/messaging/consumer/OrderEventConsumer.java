package com.ll.order.domain.messaging.consumer;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    @KafkaListener(topics = "order-event.dlq", groupId = "order-service")
    public void handleOrderDLQ(OrderEvent event) {
        if ( !event.orderEventType().toString().equals("ORDER_COMPLETED") ) {
            return;
        }
        log.error("[Order][Order Module] Received message in DLQ for OrderItemCode {}", event);
    }

    @KafkaListener(topics = "refund-event.dlq", groupId = "order-service")
    public void handleRefundDLQ(RefundEvent event) {
        log.error("[Refund][Order Module] Received message in DLQ for OrderItemCode {}", event);
    }

}
