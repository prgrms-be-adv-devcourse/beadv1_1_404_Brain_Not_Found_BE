package com.ll.payment.settlement.messaging.producer;

import com.ll.core.config.kafka.KafkaEventPublisher;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.SettlementEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementEventProducer {

    private final KafkaEventPublisher kafkaEventPublisher;

    public void sendSettlement(SettlementEvent event) {
        kafkaEventPublisher.publish("settlement-event", event);
    }

    public void sendOrder(OrderEvent event) {
        kafkaEventPublisher.publish("order-event", event);
    }

}
