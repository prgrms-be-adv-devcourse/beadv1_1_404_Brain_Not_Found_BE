package com.ll.deposit.messaging.producer;

import com.ll.core.model.vo.kafka.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(OrderEvent event) {
        kafkaTemplate.send("order-event", event);
    }

}
