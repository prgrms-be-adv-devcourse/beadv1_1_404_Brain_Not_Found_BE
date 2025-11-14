package com.example.settlement.messaging.producer;

import com.example.core.model.vo.kafka.SettlementCompleteEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String TOPIC = "settlement-event";

    public void send(SettlementCompleteEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }

}
