package com.ll.user.messaging.producer;

import com.ll.core.model.vo.kafka.UserCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDeposit(Long userId, String userCode) {
        kafkaTemplate.send("user-create-event", UserCreateEvent.depositTriggerFrom(userId, userCode));
    }

    public void sendCart(Long userId, String userCode) {
        kafkaTemplate.send("user-create-event", UserCreateEvent.cartTriggerFrom(userId, userCode));
    }

}
