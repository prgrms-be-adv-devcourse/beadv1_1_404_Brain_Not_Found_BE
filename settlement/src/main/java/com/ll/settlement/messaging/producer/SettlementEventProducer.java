package com.ll.settlement.messaging.producer;

import com.ll.core.model.vo.kafka.SettlementEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${custom.kafka.topic.settlement-event:settlement-event}")
    private String SETTLEMENT_TOPIC;

    public void send(SettlementEvent event) {
        kafkaTemplate.send(SETTLEMENT_TOPIC, event);
    }

}
