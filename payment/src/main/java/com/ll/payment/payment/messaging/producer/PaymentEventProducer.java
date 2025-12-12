package com.ll.payment.payment.messaging.producer;

import com.ll.core.config.kafka.KafkaEventPublisher;
import com.ll.core.model.vo.kafka.PaymentRefundNotificationEvent;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaEventPublisher kafkaEventPublisher;

    @Retry(name = "paymentEventProducer")
    public void sendPaymentRefundNotification(PaymentRefundNotificationEvent event) {
        log.debug("환불 알림 이벤트 발행 시도 - orderCode: {}, status: {}", 
                event.orderCode(), event.status());
        kafkaEventPublisher.publish("payment-refund-notification-event", event);
    }
}

