package com.ll.order.domain.messaging.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.PaymentRefundNotificationEvent;
import com.ll.order.domain.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "payment-refund-notification-event", groupId = "order-service")
    @Transactional
    public void handlePaymentRefundNotification(KafkaEventEnvelope<PaymentRefundNotificationEvent> event) {
        PaymentRefundNotificationEvent notificationEvent = event.payload();
        log.debug("[환불 알림 이벤트] 소비 시작 - orderCode: {}, status: {}", 
                notificationEvent.orderCode(), notificationEvent.status());

        try {
            orderService.handlePaymentRefundNotification(
                    notificationEvent.orderCode(), 
                    notificationEvent.status()
            );
            log.debug("[환불 알림 이벤트] 소비 완료 - orderCode: {}, status: {}", 
                    notificationEvent.orderCode(), notificationEvent.status());
        } catch (Exception e) {
            log.error("[환불 알림 이벤트] 소비 실패 - orderCode: {}, status: {}, error: {}", 
                    notificationEvent.orderCode(), notificationEvent.status(), e.getMessage(), e);
            // 재시도를 위해 예외를 다시 던짐
            throw e;
        }
    }

    @KafkaListener(topics = "payment-refund-notification-event.dlq", groupId = "order-service")
    public void handlePaymentRefundNotificationDLQ(KafkaEventEnvelope<PaymentRefundNotificationEvent> event) {
        log.error("[환불 알림 이벤트 DLQ] 처리 - orderCode: {}, status: {}",
                event.payload().orderCode(), event.payload().status());
        // DLQ 메시지 처리 로직 (수동 개입, 알림 등)
    }
}

