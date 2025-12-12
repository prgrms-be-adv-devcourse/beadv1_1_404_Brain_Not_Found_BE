package com.ll.payment.payment.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.vo.kafka.PaymentRefundNotificationEvent;
import com.ll.payment.payment.messaging.producer.PaymentEventProducer;
import com.ll.payment.payment.model.entity.event.PaymentRefundNotificationOutbox;
import com.ll.payment.payment.model.enums.PaymentOutboxStatus;
import com.ll.payment.payment.model.vo.PaymentRefundNotificationPayload;
import com.ll.payment.payment.repository.PaymentRefundNotificationOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRefundNotificationOutboxService {

    private final PaymentRefundNotificationOutboxRepository paymentRefundNotificationOutboxRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${payment.outbox.max-retry-count:5}")
    private Integer maxRetryCount;

    // PENDING 상태의 알림을 주문 서비스에 전송
    @Transactional
    public int publishPendingNotifications() {
        return publishNotificationsByStatus(PaymentOutboxStatus.PENDING, "발행");
    }

    // FAILED 상태의 알림을 재전송
    @Transactional
    public int republishFailedNotifications() {
        return publishNotificationsByStatus(PaymentOutboxStatus.FAILED, "재발행");
    }

    // 상태별 알림 전송 공통 메서드
    @Transactional
    public int publishNotificationsByStatus(PaymentOutboxStatus status, String action) {
        List<PaymentRefundNotificationOutbox> notifications = paymentRefundNotificationOutboxRepository
                .findByStatusAndRetryCountLessThan(status, maxRetryCount);

        if (notifications.isEmpty()) {
            log.debug("{}할 {} 상태의 환불 알림이 없습니다.", action, status);
            return 0;
        }

        log.debug("{} 상태의 환불 알림 {} 시작 - 대상: {}개", status, action, notifications.size());

        int successCount = 0;
        int failureCount = 0;

        for (PaymentRefundNotificationOutbox outbox : notifications) {
            try {
                publishNotification(outbox);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                PaymentRefundNotificationPayload payload = extractPayload(outbox);
                log.error("환불 알림 {} 실패 - outboxId: {}, orderCode: {}, error: {}",
                        action, outbox.getId(), payload != null ? payload.orderCode() : "unknown", e.getMessage(), e);
            }
        }

        log.debug("환불 알림 {} 완료 - 성공: {}개, 실패: {}개", action, successCount, failureCount);
        return successCount;
    }

    // 알림을 Kafka로 order에게 발행
    @Transactional
    public void publishNotification(PaymentRefundNotificationOutbox outbox) {
        try {
            PaymentRefundNotificationPayload payload;
            try {
                payload = objectMapper.readValue(outbox.getNotificationPayload(), PaymentRefundNotificationPayload.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("PaymentRefundNotificationPayload 역직렬화 실패 - outboxId: " + outbox.getId(), e);
            }
            outbox.markAsPublished();

            PaymentRefundNotificationEvent event = PaymentRefundNotificationEvent.of(
                    payload.orderCode(), 
                    payload.status()
            );
            paymentEventProducer.sendPaymentRefundNotification(event);

            log.debug("환불 알림 이벤트 발행 성공 - outboxId: {}, orderCode: {}, status: {}, retryCount: {}",
                    outbox.getId(), payload.orderCode(), payload.status(), outbox.getRetryCount());
        } catch (Exception e) {
            outbox.incrementRetryCount(e.getMessage());

            if (outbox.getRetryCount() >= maxRetryCount) {
                outbox.markAsFailed("최대 재시도 횟수 초과: " + e.getMessage());
                PaymentRefundNotificationPayload payload = extractPayload(outbox);
                log.warn("환불 알림 이벤트 발행 최대 재시도 횟수 초과 - outboxId: {}, orderCode: {}, retryCount: {}",
                        outbox.getId(), payload != null ? payload.orderCode() : "unknown", outbox.getRetryCount());
            } else {
                outbox.markAsFailed(e.getMessage());
            }

            paymentRefundNotificationOutboxRepository.save(outbox);
            throw e;
        }
    }

    public long countPublishableNotifications() {
        return paymentRefundNotificationOutboxRepository
                .findByStatusAndRetryCountLessThan(PaymentOutboxStatus.PENDING, maxRetryCount)
                .size();
    }

    public long countRepublishableNotifications() {
        return paymentRefundNotificationOutboxRepository
                .findByStatusAndRetryCountLessThan(PaymentOutboxStatus.FAILED, maxRetryCount)
                .size();
    }

    // 알림을 Outbox에 저장 (PENDING 상태로 저장하여 스케줄러가 전송)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToOutbox(String orderCode, String statusValue) {
        try {
            PaymentRefundNotificationOutbox outbox = PaymentRefundNotificationOutbox.from(
                    orderCode, statusValue, objectMapper);
            paymentRefundNotificationOutboxRepository.save(outbox);

            log.debug("환불 알림 Outbox 저장 완료 (PENDING) - orderCode: {}, status: {}, outboxId: {}",
                    orderCode, statusValue, outbox.getId());
        } catch (Exception e) {
            log.error("환불 알림 Outbox 저장 실패 - orderCode: {}, status: {}, error: {}",
                    orderCode, statusValue, e.getMessage(), e);
        }
    }

    private PaymentRefundNotificationPayload extractPayload(PaymentRefundNotificationOutbox outbox) {
        try {
            return objectMapper.readValue(outbox.getNotificationPayload(), PaymentRefundNotificationPayload.class);
        } catch (Exception e) {
            log.warn("PaymentRefundNotificationPayload 추출 실패 - outboxId: {}", outbox.getId(), e);
            return null;
        }
    }
}

