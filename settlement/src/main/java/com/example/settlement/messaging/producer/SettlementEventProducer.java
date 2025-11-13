package com.example.settlement.messaging.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String TOPIC = "settlement-events";

    public void send(Object event) {
        kafkaTemplate.send(TOPIC, event);
    }

}
