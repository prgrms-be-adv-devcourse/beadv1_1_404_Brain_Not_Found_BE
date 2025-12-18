package com.ll.order.domain.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.vo.kafka.PaymentRefundRequestEvent;
import com.ll.order.domain.model.entity.event.PaymentRefundEventOutbox;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.enums.order.OutboxStatus;
import com.ll.order.domain.repository.PaymentRefundRequestEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRefundRequestEventOutboxService {

    private final PaymentRefundRequestEventOutboxRepository paymentRefundRequestEventOutboxRepository;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${order.outbox.max-retry-count:5}")
    private Integer maxRetryCount;

    // PENDING 상태의 이벤트를 Kafka에 발행
    @Transactional
    public int publishPendingEvents() {
        return publishEventsByStatus(OutboxStatus.PENDING, "발행");
    }

    // FAILED 상태의 이벤트를 재발행
    @Transactional
    public int republishFailedEvents() {
        return publishEventsByStatus(OutboxStatus.FAILED, "재발행");
    }

    // 상태별 이벤트 발행 공통 메서드
    @Transactional
    public int publishEventsByStatus(OutboxStatus status, String action) {
        List<PaymentRefundEventOutbox> events = paymentRefundRequestEventOutboxRepository
                .findByStatusAndRetryCountLessThan(status, maxRetryCount);

        if (events.isEmpty()) {
            log.debug("{}할 {} 상태의 환불 요청 이벤트가 없습니다.", action, status);
            return 0;
        }

        log.debug("{} 상태의 환불 요청 이벤트 {} 시작 - 대상: {}개", status, action, events.size());

        int successCount = 0;
        int failureCount = 0;

        for (PaymentRefundEventOutbox outbox : events) {
            try {
                publishEvent(outbox);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("환불 요청 이벤트 {} 실패 - outboxId: {}, orderCode: {}, error: {}",
                        action, outbox.getId(), outbox.getOrderCode(), e.getMessage(), e);
            }
        }

        log.debug("환불 요청 이벤트 {} 완료 - 성공: {}개, 실패: {}개", action, successCount, failureCount);
        return successCount;
    }

    // 이벤트를 Kafka에 발행
    @Transactional
    public void publishEvent(PaymentRefundEventOutbox outbox) {
        try {
            PaymentRefundRequestEvent refundRequestEvent;
            try {
                refundRequestEvent = objectMapper.readValue(outbox.getEventPayload(), PaymentRefundRequestEvent.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("PaymentRefundRequestEvent 역직렬화 실패 - outboxId: " + outbox.getId(), e);
            }
            outbox.markAsPublished();

            orderEventProducer.sendPaymentRefundRequest(refundRequestEvent);

            log.debug("환불 요청 이벤트 발행 성공 - outboxId: {}, orderCode: {}, retryCount: {}",
                    outbox.getId(), outbox.getOrderCode(), outbox.getRetryCount());

        } catch (Exception e) {
            outbox.incrementRetryCount(e.getMessage());

            if (outbox.getRetryCount() >= maxRetryCount) {
                outbox.markAsFailed("최대 재시도 횟수 초과: " + e.getMessage());
                log.warn("환불 요청 이벤트 발행 최대 재시도 횟수 초과 - outboxId: {}, orderCode: {}, retryCount: {}",
                        outbox.getId(), outbox.getOrderCode(), outbox.getRetryCount());
            } else {
                outbox.markAsFailed(e.getMessage());
            }

            paymentRefundRequestEventOutboxRepository.save(outbox);
            throw e;
        }
    }

    public long countPublishableEvents() {
        return paymentRefundRequestEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.PENDING, maxRetryCount)
                .size();
    }

    public long countRepublishableEvents() {
        return paymentRefundRequestEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.FAILED, maxRetryCount)
                .size();
    }

    // 이벤트를 Outbox에 저장 (PENDING 상태로 저장하여 스케줄러가 발행)
    @Transactional
    public void saveToOutbox(PaymentRefundRequestEvent event, String orderCode) {
        try {
            PaymentRefundEventOutbox outbox = PaymentRefundEventOutbox.from(event, orderCode, objectMapper);
            paymentRefundRequestEventOutboxRepository.save(outbox);

            log.debug("환불 요청 이벤트 Outbox 저장 완료 (PENDING) - orderCode: {}, orderId: {}, outboxId: {}",
                    orderCode, event.orderId(), outbox.getId());
        } catch (Exception e) {
            log.error("환불 요청 이벤트 Outbox 저장 실패 - orderCode: {}, orderId: {}, error: {}",
                    orderCode, event.orderId(), e.getMessage(), e);
        }
    }
}

