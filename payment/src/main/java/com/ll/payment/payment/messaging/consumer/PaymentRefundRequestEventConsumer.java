package com.ll.payment.payment.messaging.consumer;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.PaymentRefundRequestEvent;
import com.ll.payment.payment.exception.PaymentErrorCode;
import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.payment.repository.PaymentJpaRepository;
import com.ll.payment.payment.service.refund.PaymentRefundService;
import com.ll.payment.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRefundRequestEventConsumer {

    private final PaymentRefundService paymentRefundService;
    private final PaymentJpaRepository paymentJpaRepository;
    private final SettlementService settlementService;

    @KafkaListener(topics = "payment-refund-request-event", groupId = "payment-service")
    @Transactional
    public void handlePaymentRefundRequestEvent(KafkaEventEnvelope<PaymentRefundRequestEvent> event) {
        PaymentRefundRequestEvent requestEvent = event.payload();
        log.debug("[환불 요청 이벤트] 소비 시작 - orderCode: {}, refundAmount: {}",
                requestEvent.orderCode(), requestEvent.refundAmount());

        try {
            // 정산 환불 처리
            settlementService.refundSettlement(requestEvent);

            // Payment 엔티티 조회 (COMPLETED 상태의 결제만 환불 가능)
            Payment payment = paymentJpaRepository.findByOrderIdAndPaymentStatus(
                            requestEvent.orderId(),
                            PaymentStatus.COMPLETED
                    )
                    .orElseThrow(() -> {
                        log.warn("[환불 요청 이벤트] 환불 대상 결제 정보를 찾을 수 없습니다. orderId: {}, status: COMPLETED", requestEvent.orderId());
                        return new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                    });

            PaymentRefundRequest request = new PaymentRefundRequest(
                    payment.getId(),
                    payment.getCode(),
                    requestEvent.orderId(),
                    requestEvent.orderCode(),
                    requestEvent.refundAmount(),
                    requestEvent.reason(),
                    payment.getPaidType(), // 조회된 Payment의 PaidType 사용
                    requestEvent.buyerCode()
            );

            paymentRefundService.refundPayment(request);
            log.debug("[환불 요청 이벤트] 소비 완료 - orderCode: {}, refundAmount: {}",
                    requestEvent.orderCode(), requestEvent.refundAmount());

        } catch (Exception e) {
            log.error("[환불 요청 이벤트] 소비 실패 - orderCode: {}, error: {}",
                    requestEvent.orderCode(), e.getMessage(), e);
            // 재시도를 위해 예외를 다시 던짐
            throw e;
        }
    }

    @KafkaListener(topics = "payment-refund-request-event.dlq", groupId = "payment-service")
    public void handlePaymentRefundRequestDLQ(KafkaEventEnvelope<PaymentRefundRequestEvent> event) {
        log.error("[환불 요청 이벤트 DLQ] 처리 - orderCode: {}, eventId: {}",
                event.payload().orderCode(), event.eventId());
        // DLQ 메시지 처리 로직 (수동 개입, 알림 등)
    }
}

