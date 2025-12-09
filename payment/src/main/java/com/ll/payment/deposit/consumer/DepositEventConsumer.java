package com.ll.payment.deposit.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.UserCreateEvent;
import com.ll.core.model.vo.kafka.enums.UserCreateEventType;
import com.ll.payment.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositEventConsumer {

    private final DepositService depositService;

    @KafkaListener(topics = "user-create-event", groupId = "deposit-service")
    public void handleUserCreateEvent(KafkaEventEnvelope<UserCreateEvent> event) {
        if ( event.payload().eventType() != UserCreateEventType.DEPOSIT_CREATE ) {
            return;
        }
        depositService.createDeposit(event.payload().userCode());
    }

    @KafkaListener(topics = "user-create-event.dlq", groupId = "deposit-service")
    public void handleUserCreateDLQ(KafkaEventEnvelope<UserCreateEvent> event) {
        if ( event.payload().eventType() != UserCreateEventType.DEPOSIT_CREATE ) {
            return;
        }
    }
}
