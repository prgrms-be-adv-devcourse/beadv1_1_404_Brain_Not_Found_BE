package com.ll.deposit.messaging.consumer;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.SettlementEvent;
import com.ll.core.model.vo.kafka.UserCreateEvent;
import com.ll.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.deposit.service.DepositService;
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
    public void handleUserCreateEvent(UserCreateEvent event) {
        if ( !event.eventType().toString().equals("DEPOSIT_CREATE") ) {
            return;
        }
        log.info("[UserCreate][Deposit Module] Received UserCreate from User service : {}", event);
        depositService.createDeposit(event.userCode());
    }

    @KafkaListener(topics = "user-create-event.dlq", groupId = "deposit-service")
    public void handleUserCreateDLQ(UserCreateEvent event) {
        if ( !event.eventType().toString().equals("DEPOSIT_CREATE") ) {
            return;
        }
        log.error("[UserCreate][Deposit Module] Received message in DLQ for UserCode {}", event.userCode());
    }

//    @KafkaListener(topics = "order-event", groupId = "deposit-service")
//    public void handleOrderEvent(OrderEvent event) {
//        if ( !event.orderEventType().toString().equals("SETTLEMENT_COMPLETED") ) {
//            return;
//        }
//        log.info("[Order][Deposit Module] Received OrderEvent from Order service : {}", event);
//        depositService.paymentDeposit(event);
//    }

//    @KafkaListener(topics = "order-event.dlq", groupId = "deposit-service")
//    public void handleOrderDLQ(OrderEvent event) {
//        if ( !event.orderEventType().toString().equals("SETTLEMENT_COMPLETED") ) {
//            return;
//        }
//        log.error("[Order][Deposit Module] Received message in DLQ for OrderItemCode {}", event);
//    }

    @KafkaListener(topics = "settlement-event", groupId = "deposit-service")
    public void handleSettlementEvent(@Valid SettlementEvent event) {
        log.info("[Settlement][Deposit Module] Received SettlementEvent from Settlement service : {}", event);
        depositService.chargeDeposit(event.sellerCode(), DepositTransactionRequest.of(event.amount(), settlementCompleteReferenceFormatter.apply(event)));
    }

    private final Function<SettlementEvent, String> settlementCompleteReferenceFormatter =
            event -> String.format("Settlement Complete | orderItemCode : %s | amount : %d", event.orderItemCode(), event.amount());

}
