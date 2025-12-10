package com.ll.order.domain.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.vo.kafka.RefundEvent;
import com.ll.order.domain.model.entity.event.RefundEventOutbox;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.enums.order.OutboxStatus;
import com.ll.order.domain.repository.RefundEventOutboxRepository;
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
public class RefundEventOutboxService {

    private final RefundEventOutboxRepository refundEventOutboxRepository;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${order.outbox.max-retry-count:5}")
    private Integer maxRetryCount;

    // PENDING 상태의 이벤트를 Kafka에 발행
    @Transactional // 없으면 self invocation 경고 발생
    public int publishPendingEvents() {
        List<RefundEventOutbox> pendingEvents = refundEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.PENDING, maxRetryCount);

        if (pendingEvents.isEmpty()) {
            log.debug("발행할 PENDING 상태의 환불 이벤트가 없습니다.");
            return 0;
        }

        log.debug("PENDING 상태의 환불 이벤트 발행 시작 - 대상: {}개", pendingEvents.size());

        int successCount = 0;
        int failureCount = 0;

        for (RefundEventOutbox outbox : pendingEvents) {
            try {
                publishEvent(outbox);  // Self injection을 통해 프록시를 거쳐 호출
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("환불 이벤트 발행 실패 - outboxId: {}, referenceCode: {}, error: {}",
                        outbox.getId(), outbox.getReferenceCode(), e.getMessage(), e);
            }
        }

        log.debug("환불 이벤트 발행 완료 - 성공: {}개, 실패: {}개", successCount, failureCount);
        return successCount;
    }

    // FAILED 상태의 이벤트를 재발행
    @Transactional
    public int republishFailedEvents() {
        List<RefundEventOutbox> failedEvents = refundEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.FAILED, maxRetryCount);

        if (failedEvents.isEmpty()) {
            log.debug("재발행할 실패한 환불 이벤트가 없습니다.");
            return 0;
        }

        log.debug("실패한 환불 이벤트 재발행 시작 - 대상: {}개", failedEvents.size());

        int successCount = 0;
        int failureCount = 0;

        for (RefundEventOutbox outbox : failedEvents) {
            try {
                publishEvent(outbox);  // Self injection을 통해 프록시를 거쳐 호출
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("환불 이벤트 재발행 실패 - outboxId: {}, referenceCode: {}, error: {}",
                        outbox.getId(), outbox.getReferenceCode(), e.getMessage(), e);
            }
        }

        log.debug("환불 이벤트 재발행 완료 - 성공: {}개, 실패: {}개", successCount, failureCount);
        return successCount;
    }

    // 이벤트를 Kafka에 발행
    @Transactional
    public void publishEvent(RefundEventOutbox outbox) {
        try {
            RefundEvent refundEvent;
            try {
                refundEvent = objectMapper.readValue(outbox.getEventPayload(), RefundEvent.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("RefundEvent 역직렬화 실패 - outboxId: " + outbox.getId(), e);
            }

            orderEventProducer.sendRefund(refundEvent);

            outbox.markAsPublished();
            refundEventOutboxRepository.save(outbox);

            log.debug("환불 이벤트 발행 성공 - outboxId: {}, referenceCode: {}, retryCount: {}",
                    outbox.getId(), outbox.getReferenceCode(), outbox.getRetryCount());

        } catch (Exception e) {
            outbox.incrementRetryCount(e.getMessage());

            if (outbox.getRetryCount() >= maxRetryCount) {
                outbox.markAsFailed("최대 재시도 횟수 초과: " + e.getMessage());
                log.warn("환불 이벤트 발행 최대 재시도 횟수 초과 - outboxId: {}, referenceCode: {}, retryCount: {}",
                        outbox.getId(), outbox.getReferenceCode(), outbox.getRetryCount());
            } else {
                outbox.markAsFailed(e.getMessage());
            }

            refundEventOutboxRepository.save(outbox);
            throw e;
        }
    }

    public long countPublishableEvents() {
        return refundEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.PENDING, maxRetryCount)
                .size();
    }

    public long countRepublishableEvents() {
        return refundEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.FAILED, maxRetryCount)
                .size();
    }

    // 이벤트를 Outbox에 저장 (PENDING 상태로 저장하여 스케줄러가 발행)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToOutbox(RefundEvent refundEvent, String orderCode) {
        try {
            RefundEventOutbox outbox = RefundEventOutbox.from(refundEvent, orderCode, objectMapper);
            refundEventOutboxRepository.save(outbox);

            log.debug("환불 이벤트 Outbox 저장 완료 (PENDING) - orderCode: {}, orderItemCode: {}, referenceCode: {}, outboxId: {}",
                    orderCode, refundEvent.orderItemCode(), refundEvent.referenceCode(), outbox.getId());
        } catch (Exception e) {
            log.error("환불 이벤트 Outbox 저장 실패 - orderCode: {}, orderItemCode: {}, referenceCode: {}, error: {}",
                    orderCode, refundEvent.orderItemCode(), refundEvent.referenceCode(), e.getMessage(), e);
        }
    }
}

