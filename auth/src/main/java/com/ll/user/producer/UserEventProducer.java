package com.ll.user.producer;

import com.ll.core.config.kafka.KafkaEventPublisher;
import com.ll.core.model.vo.kafka.UserCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaEventPublisher kafkaEventPublisher;

    public void sendDeposit(Long userId, String userCode) {
        kafkaEventPublisher.publish("user-create-event", UserCreateEvent.depositTriggerFrom(userId, userCode));
    }

    public void sendCart(Long userId, String userCode) {
        kafkaEventPublisher.publish("user-create-event", UserCreateEvent.cartTriggerFrom(userId, userCode));
    }

}
