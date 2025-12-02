package com.ll.cart.messaging.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.UserCreateEvent;
import com.ll.core.model.vo.kafka.enums.UserCreateEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventConsumer {

    @KafkaListener(topics = "user-create-event", groupId = "cart-service")
    public void handleUserCreateEvent(KafkaEventEnvelope<UserCreateEvent> event) {
        if ( event.payload().eventType() != UserCreateEventType.CART_CREATE ) {
            return;
        }
    }

    @KafkaListener(topics = "user-create-event.dlq", groupId = "cart-service")
    public void handleUserCreateDLQ(KafkaEventEnvelope<UserCreateEvent> event) {
        if ( event.payload().eventType() != UserCreateEventType.CART_CREATE ) {
            return;
        }
    }

}
