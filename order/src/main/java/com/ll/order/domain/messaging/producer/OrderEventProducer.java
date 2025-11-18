package com.ll.order.domain.messaging.producer;

import com.fasterxml.uuid.Generators;
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

    public void sendInventoryDecrease(String productCode, int quantity) {
        String referenceCode = Generators.timeBasedEpochGenerator().generate().toString();
        kafkaTemplate.send("inventory-event", InventoryEvent.stockDecreaseEvent(productCode, quantity, referenceCode));
    }

    public void sendInventoryRollback(String productCode, int quantity) {
        String referenceCode = Generators.timeBasedEpochGenerator().generate().toString();
        kafkaTemplate.send("inventory-event", InventoryEvent.stockRollbackEvent(productCode, quantity, referenceCode));
    }
}
