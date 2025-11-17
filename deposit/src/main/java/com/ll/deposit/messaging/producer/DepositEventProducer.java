package com.ll.deposit.messaging.producer;

import com.ll.core.model.vo.kafka.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${custom.kafka.topic.order-event:order-event}")
    private String ORDER_TOPIC;

    public void send(OrderEvent event) {
        kafkaTemplate.send(ORDER_TOPIC, event);
    }

}
