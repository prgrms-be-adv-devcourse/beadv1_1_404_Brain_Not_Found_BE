package com.ll.cart.messaging.consumer;

import com.ll.core.model.vo.kafka.UserCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventConsumer {

    @KafkaListener(topics = "user-create-event", groupId = "cart-service")
    public void handleUserCreateEvent(UserCreateEvent event) {
        if ( !event.eventType().toString().equals("CART_CREATE") ) {
            return;
        }
        log.info("[UserCreate][Cart Module] Received UserCreate from User service : {}", event);
    }

    @KafkaListener(topics = "user-create-event.dlq", groupId = "deposit-service")
    public void handleUserCreateDLQ(UserCreateEvent event) {
        if ( !event.eventType().toString().equals("CART_CREATE") ) {
            return;
        }
        log.error("[UserCreate][Cart Module] Received message in DLQ for UserCode {}", event.userCode());
    }

}
