package com.ll.payment.deposit.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.SettlementEvent;
import com.ll.core.model.vo.kafka.UserCreateEvent;
import com.ll.core.model.vo.kafka.enums.UserCreateEventType;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.function.Function;

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

    @KafkaListener(topics = "settlement-event", groupId = "deposit-service")
    public void handleSettlementEvent(@Valid KafkaEventEnvelope<SettlementEvent> event) {
        depositService.chargeDeposit(event.payload().sellerCode(), DepositTransactionRequest.of(event.payload().amount(), settlementCompleteReferenceFormatter.apply(event.payload())));
    }

    private final Function<SettlementEvent, String> settlementCompleteReferenceFormatter =
            event -> String.format("Settlement Complete | orderItemCode : %s | amount : %d", event.orderItemCode(), event.amount());

}
