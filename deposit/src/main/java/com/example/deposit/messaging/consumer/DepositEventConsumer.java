package com.example.deposit.messaging.consumer;

import com.example.core.model.vo.kafka.SettlementCompleteEvent;
import com.example.deposit.model.vo.request.DepositTransactionRequest;
import com.example.deposit.service.DepositService;
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

    @KafkaListener(topics = "settlement-event", groupId = "deposit-service")
    public void handleSettlementCompleteEvent(@Valid SettlementCompleteEvent event) {
        if ( event.type().toString().equals("SETTLEMENT") ) {
            depositService.chargeDeposit(event.sellerCode(), DepositTransactionRequest.of(event.amount(), settlementCompleteReferenceFormatter.apply(event)));
        } else if ( event.type().toString().equals("REFUND") ) {
            // 환불 처리 로직 추가 예정
            log.info("Received REFUND event for sellerCode {}: amount={}, referenceCode={}", event.sellerCode(), event.amount(), settlementCompleteReferenceFormatter.apply(event));
        }
    }

    private final Function<SettlementCompleteEvent, String> settlementCompleteReferenceFormatter =
            event -> String.format("%s Complete | orderItemCode : %s | amount : %d", event.type().toString(), event.orderItemCode(), event.amount());

    private final Function<SettlementCompleteEvent, String> settlementFailReferenceFormatter =
            event -> String.format("%s Fail | orderItemCode : %s | amount : %d", event.type().toString(), event.orderItemCode(), event.amount());

}
