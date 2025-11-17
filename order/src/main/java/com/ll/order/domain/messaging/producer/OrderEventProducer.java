package com.ll.order.domain.messaging.producer;

import com.ll.core.model.vo.kafka.InventoryEvent;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrder(OrderEvent event) {
        kafkaTemplate.send("order-event", event);
    }

    public void sendRefund(RefundEvent event) {
        kafkaTemplate.send("refund-event", event);
    }

    public void sendInventory(InventoryEvent event) {
        kafkaTemplate.send("inventory-event", event);
    }

}
