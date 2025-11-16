package com.example.deposit.messaging.producer;

import com.example.core.model.vo.kafka.SettlementRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "order-event";

    public void send(SettlementRequestEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }

}
