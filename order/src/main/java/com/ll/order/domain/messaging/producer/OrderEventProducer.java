package com.ll.order.domain.messaging.producer;

import com.ll.core.model.vo.kafka.InventoryEvent;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.RefundEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${custom.kafka.topic.order-event:order-event}")
    private String ORDER_TOPIC;
    @Value("${custom.kafka.topic.refund-event:refund-event}")
    private String REFUND_TOPIC;
    @Value("${custom.kafka.topic.inventory-event:inventory-event}")
    private String INVENTORY_TOPIC;


    public void sendOrder(OrderEvent event) {
        kafkaTemplate.send(ORDER_TOPIC, event);
    }

    public void sendRefund(RefundEvent event) {
        kafkaTemplate.send(REFUND_TOPIC, event);
    }

    public void sendInventory(InventoryEvent event) {
        kafkaTemplate.send(INVENTORY_TOPIC, event);
    }

}
