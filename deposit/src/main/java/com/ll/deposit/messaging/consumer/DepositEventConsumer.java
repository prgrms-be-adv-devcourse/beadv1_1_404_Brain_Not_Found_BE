package com.ll.deposit.messaging.consumer;

import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.core.model.vo.kafka.SettlementEvent;
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

//    @KafkaListener(topics = "user-events", groupId = "deposit-service")
//    public void handleUserCreateEvent(UserCreateEvent event) {
//        try {
//            depositService.createDeposit(event.userCode());
//        } catch (Exception e) {
//            log.error("Failed to process UserCreateEvent for userId {}: {}", event.userId(), e.getMessage());
//            // TODO: 보상 처리 로직 추가 ( Dead Letter Queue 는 KafkaConfig 에서 설정 완료 )
//        }
//    }
//
//    @KafkaListener(topics = "payment-events", groupId = "deposit-service")
//    public void handlePaymentEvent(DepositChargeEvent event) {
//        try {
//            depositService.chargeDeposit(event.userCode(), DepositTransactionRequest.of(event.amount(), event.referenceCode()));
//        } catch (Exception e) {
//            log.error("Failed to process DepositChargeEvent for userId {}: {}", event.userId(), e.getMessage());
//            // TODO: 보상 처리 로직 추가 ( Dead Letter Queue 는 KafkaConfig 에서 설정 완료 )
//        }
//    }

    @KafkaListener(topics = "order-event", groupId = "deposit-service")
    public void handleOrderEvent(OrderEvent event) {
        if ( !event.orderEventType().toString().equals("SETTLEMENT_COMPLETED") ) {
            return;
        }
        depositService.paymentDeposit(event);
    }

    @KafkaListener(topics = "settlement-event", groupId = "deposit-service")
    public void handleSettlementEvent(@Valid SettlementEvent event) {
        depositService.chargeDeposit(event.sellerCode(), DepositTransactionRequest.of(event.amount(), settlementCompleteReferenceFormatter.apply(event)));
    }

    private final Function<SettlementEvent, String> settlementCompleteReferenceFormatter =
            event -> String.format("Settlement Complete | orderItemCode : %s | amount : %d", event.orderItemCode(), event.amount());

}
