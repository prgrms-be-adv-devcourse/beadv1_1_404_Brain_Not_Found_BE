package com.ll.settlement.messaging.producer;

import com.ll.core.model.vo.kafka.SettlementEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String TOPIC = "settlement-event";

    public void send(SettlementEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }

}
