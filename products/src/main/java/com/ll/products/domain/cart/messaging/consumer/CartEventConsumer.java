package com.ll.products.domain.cart.messaging.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.UserCreateEvent;
import com.ll.core.model.vo.kafka.enums.UserCreateEventType;
import com.ll.products.domain.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventConsumer {

    private final CartService cartService;

    @KafkaListener(topics = "user-create-event", groupId = "cart-service")
    public void handleUserCreateEvent(KafkaEventEnvelope<UserCreateEvent> event) {
        if ( event.payload().eventType() != UserCreateEventType.CART_CREATE ) {
            cartService.createCartForNewUser(event.payload());
        }
    }

    @KafkaListener(topics = "user-create-event.dlq", groupId = "cart-service")
    public void handleUserCreateDLQ(KafkaEventEnvelope<UserCreateEvent> event) {
        if ( event.payload().eventType() != UserCreateEventType.CART_CREATE ) {
            return;
        }
    }

}
