package com.ll.order.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.vo.kafka.OrderEvent;
import com.ll.order.domain.model.entity.OrderEventOutbox;
import com.ll.order.domain.model.entity.OrderEventOutbox.OutboxStatus;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.repository.OrderEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
                republishEvent(outbox);
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

    @Transactional
    public void republishEvent(OrderEventOutbox outbox) {
        try {
            // JSON을 OrderEvent로 역직렬화
            OrderEvent orderEvent;
            try {
                orderEvent = objectMapper.readValue(outbox.getEventPayload(), OrderEvent.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("OrderEvent 역직렬화 실패 - outboxId: " + outbox.getId(), e);
            }

            // 이벤트 재발행 시도
            orderEventProducer.sendOrder(orderEvent);

            // 재발행 성공 시 상태 변경
            outbox.markAsPublished();
            orderEventOutboxRepository.save(outbox);

            log.debug("이벤트 재발행 성공 - outboxId: {}, referenceCode: {}, retryCount: {}",
                    outbox.getId(), outbox.getReferenceCode(), outbox.getRetryCount());

        } catch (Exception e) {
            // 재발행 실패 시 재시도 횟수 증가
            outbox.incrementRetryCount(e.getMessage());

            // 최대 재시도 횟수 초과 시 상태 유지 (FAILED)
            if (outbox.getRetryCount() >= maxRetryCount) {
                outbox.markAsFailed("최대 재시도 횟수 초과: " + e.getMessage());
                log.warn("이벤트 재발행 최대 재시도 횟수 초과 - outboxId: {}, referenceCode: {}, retryCount: {}",
                        outbox.getId(), outbox.getReferenceCode(), outbox.getRetryCount());
            }

            orderEventOutboxRepository.save(outbox);
            throw e; // 상위로 예외 전파
        }
    }

    public long countRepublishableEvents() {
        return orderEventOutboxRepository
                .findByStatusAndRetryCountLessThan(OutboxStatus.FAILED, maxRetryCount)
                .size();
    }
}

