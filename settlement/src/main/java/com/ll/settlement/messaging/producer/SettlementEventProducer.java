package com.ll.settlement.messaging.producer;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.SettlementEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSettlement(SettlementEvent event) {
        kafkaTemplate.send("settlement-event", event);
    }

    public void sendOrder(OrderEvent event) {
        kafkaTemplate.send("order-event", event);
    }

}
