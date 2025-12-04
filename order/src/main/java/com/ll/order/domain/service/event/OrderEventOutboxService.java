package com.ll.order.domain.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.order.domain.model.entity.OrderEventOutbox;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.model.enums.order.OutboxStatus;
import com.ll.order.domain.repository.OrderEventOutboxRepository;
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
public class OrderEventOutboxService {

    private final OrderEventOutboxRepository orderEventOutboxRepository;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${order.outbox.max-retry-count:5}")
    private Integer maxRetryCount;

    // PENDING 상태의 이벤트를 Kafka에 발행
    @Transactional
    public int publishPendingEvents() {
        // 발행 대상 조회: PENDING 상태인 이벤트
        List<OrderEventOutbox> pendingEvents = orderEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.PENDING, maxRetryCount);

        if (pendingEvents.isEmpty()) {
            log.debug("발행할 PENDING 상태의 이벤트가 없습니다.");
            return 0;
        }

        log.debug("PENDING 상태의 이벤트 발행 시작 - 대상: {}개", pendingEvents.size());

        int successCount = 0;
        int failureCount = 0;

        for (OrderEventOutbox outbox : pendingEvents) {
            try {
                publishEvent(outbox);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("이벤트 발행 실패 - outboxId: {}, referenceCode: {}, error: {}",
                        outbox.getId(), outbox.getReferenceCode(), e.getMessage(), e);
            }
        }

        log.debug("이벤트 발행 완료 - 성공: {}개, 실패: {}개", successCount, failureCount);
        return successCount;
    }

    // FAILED 상태의 이벤트를 재발행
    @Transactional
    public int republishFailedEvents() {
        // 재발행 대상 조회: FAILED 상태이고, 재시도 횟수가 최대값 미만인 것
        List<OrderEventOutbox> failedEvents = orderEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.FAILED, maxRetryCount);

        if (failedEvents.isEmpty()) {
            log.debug("재발행할 실패한 이벤트가 없습니다.");
            return 0;
        }

        log.debug("실패한 이벤트 재발행 시작 - 대상: {}개", failedEvents.size());

        int successCount = 0;
        int failureCount = 0;

        for (OrderEventOutbox outbox : failedEvents) {
            try {
                publishEvent(outbox);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("이벤트 재발행 실패 - outboxId: {}, referenceCode: {}, error: {}",
                        outbox.getId(), outbox.getReferenceCode(), e.getMessage(), e);
            }
        }

        log.debug("이벤트 재발행 완료 - 성공: {}개, 실패: {}개", successCount, failureCount);
        return successCount;
    }

    // 이벤트를 Kafka에 발행 (PENDING 또는 FAILED 상태의 이벤트 처리)
    @Transactional
    public void publishEvent(OrderEventOutbox outbox) {
        try {
            // JSON을 OrderEvent로 역직렬화
            OrderEvent orderEvent;
            try {
                orderEvent = objectMapper.readValue(outbox.getEventPayload(), OrderEvent.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("OrderEvent 역직렬화 실패 - outboxId: " + outbox.getId(), e);
            }

            // 이벤트 발행 시도
            orderEventProducer.sendOrder(orderEvent);

            // 발행 성공 시 상태 변경
            outbox.markAsPublished();
            orderEventOutboxRepository.save(outbox);

            log.debug("이벤트 발행 성공 - outboxId: {}, referenceCode: {}, retryCount: {}",
                    outbox.getId(), outbox.getReferenceCode(), outbox.getRetryCount());

        } catch (Exception e) {
            // 발행 실패 시 재시도 횟수 증가
            outbox.incrementRetryCount(e.getMessage());

            // 최대 재시도 횟수 초과 시 상태를 FAILED로 변경
            if (outbox.getRetryCount() >= maxRetryCount) {
                outbox.markAsFailed("최대 재시도 횟수 초과: " + e.getMessage());
                log.warn("이벤트 발행 최대 재시도 횟수 초과 - outboxId: {}, referenceCode: {}, retryCount: {}",
                        outbox.getId(), outbox.getReferenceCode(), outbox.getRetryCount());
            } else {
                // 재시도 횟수가 최대값 미만이면 상태를 FAILED로 변경 (다음 스케줄러 실행 시 재시도)
                outbox.markAsFailed(e.getMessage());
            }

            orderEventOutboxRepository.save(outbox);
            throw e; // 상위로 예외 전파
        }
    }

    public long countPublishableEvents() {
        return orderEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.PENDING, maxRetryCount)
                .size();
    }

    public long countRepublishableEvents() {
        return orderEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.FAILED, maxRetryCount)
                .size();
    }

    // 이벤트를 Outbox에 저장 (PENDING 상태로 저장하여 스케줄러가 발행)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToOutbox(OrderEvent orderEvent, String orderCode, String orderItemCode) {
        try {
            OrderEventOutbox outbox = OrderEventOutbox.from(orderEvent, objectMapper);
            // PENDING 상태로 저장 (기본값이므로 명시적으로 설정하지 않아도 됨)
            orderEventOutboxRepository.save(outbox);

            log.debug("주문 이벤트 Outbox 저장 완료 (PENDING) - orderCode: {}, orderItemCode: {}, referenceCode: {}, outboxId: {}",
                    orderCode, orderItemCode, orderEvent.referenceCode(), outbox.getId());
        } catch (Exception e) {
            log.error("주문 이벤트 Outbox 저장 실패 - orderCode: {}, orderItemCode: {}, referenceCode: {}, error: {}",
                    orderCode, orderItemCode, orderEvent.referenceCode(), e.getMessage(), e);
            // Outbox 저장 실패는 로그만 남기고 계속 진행 (수동 처리 필요)
        }
    }
}

