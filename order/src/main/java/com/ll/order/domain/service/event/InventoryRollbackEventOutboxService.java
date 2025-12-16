package com.ll.order.domain.service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import com.ll.core.model.vo.kafka.InventoryEvent;
import com.ll.order.domain.model.entity.event.InventoryRollbackEventOutbox;
import com.ll.order.domain.model.entity.event.InventoryRollbackEventOutbox.CompensationOutboxStatus;
import com.ll.order.domain.messaging.producer.OrderEventProducer;
import com.ll.order.domain.repository.InventoryRollbackEventOutboxRepository;
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
public class InventoryRollbackEventOutboxService {

    private final InventoryRollbackEventOutboxRepository inventoryRollbackEventOutboxRepository;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${order.outbox.max-retry-count:5}")
    private Integer maxRetryCount;

    // PENDING 상태의 이벤트를 Kafka에 발행
    @Transactional
    public int publishPendingEvents() {
        List<InventoryRollbackEventOutbox> pendingEvents = inventoryRollbackEventOutboxRepository
                .findByStatusAndRetryCountLessThan(CompensationOutboxStatus.PENDING, maxRetryCount);

        if (pendingEvents.isEmpty()) {
            log.debug("발행할 PENDING 상태의 재고 롤백 이벤트가 없습니다.");
            return 0;
        }
        log.debug("PENDING 상태의 재고 롤백 이벤트 발행 시작 - 대상: {}개", pendingEvents.size());

        int successCount = 0;
        int failureCount = 0;

        for (InventoryRollbackEventOutbox outbox : pendingEvents) {
            try {
                publishEvent(outbox);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("재고 롤백 이벤트 발행 실패 - outboxId: {}, referenceCode: {}, error: {}",
                        outbox.getId(), outbox.getReferenceCode(), e.getMessage(), e);
            }
        }

        log.debug("재고 롤백 이벤트 발행 완료 - 성공: {}개, 실패: {}개", successCount, failureCount);
        return successCount;
    }

    // FAILED 상태의 이벤트를 재발행
    @Transactional
    public int republishFailedEvents() {
        List<InventoryRollbackEventOutbox> failedEvents = inventoryRollbackEventOutboxRepository
                .findByStatusAndRetryCountLessThan(CompensationOutboxStatus.FAILED, maxRetryCount);

        if (failedEvents.isEmpty()) {
            log.debug("재발행할 실패한 재고 롤백 이벤트가 없습니다.");
            return 0;
        }
        log.debug("실패한 재고 롤백 이벤트 재발행 시작 - 대상: {}개", failedEvents.size());

        int successCount = 0;
        int failureCount = 0;

        for (InventoryRollbackEventOutbox outbox : failedEvents) {
            try {
                publishEvent(outbox);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("재고 롤백 이벤트 재발행 실패 - outboxId: {}, referenceCode: {}, error: {}",
                        outbox.getId(), outbox.getReferenceCode(), e.getMessage(), e);
            }
        }

        log.debug("재고 롤백 이벤트 재발행 완료 - 성공: {}개, 실패: {}개", successCount, failureCount);
        return successCount;
    }

    // 이벤트를 Kafka에 발행
    @Transactional
    public void publishEvent(InventoryRollbackEventOutbox outbox) {
        try {
            InventoryEvent inventoryEvent;
            try {
                inventoryEvent = objectMapper.readValue(outbox.getEventPayload(), InventoryEvent.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("InventoryEvent 역직렬화 실패 - outboxId: " + outbox.getId(), e);
            }

            outbox.markAsPublished();
            inventoryRollbackEventOutboxRepository.save(outbox);

            orderEventProducer.sendInventoryRollback(
                    inventoryEvent.productCode(),
                    inventoryEvent.quantity(),
                    inventoryEvent.referenceCode()
            );

            log.debug("재고 롤백 이벤트 발행 성공 - outboxId: {}, referenceCode: {}, retryCount: {}",
                    outbox.getId(), outbox.getReferenceCode(), outbox.getRetryCount());
        } catch (Exception e) {
            outbox.incrementRetryCount(e.getMessage());

            if (outbox.getRetryCount() >= maxRetryCount) {
                outbox.markAsFailed("최대 재시도 횟수 초과: " + e.getMessage());
                log.warn("재고 롤백 이벤트 발행 최대 재시도 횟수 초과 - outboxId: {}, referenceCode: {}, retryCount: {}",
                        outbox.getId(), outbox.getReferenceCode(), outbox.getRetryCount());
            } else {
                outbox.markAsFailed(e.getMessage());
            }

            inventoryRollbackEventOutboxRepository.save(outbox);
            throw e;
        }
    }

    public long countPublishableEvents() {
        return inventoryRollbackEventOutboxRepository
                .findByStatusAndRetryCountLessThan(CompensationOutboxStatus.PENDING, maxRetryCount)
                .size();
    }

    public long countRepublishableEvents() {
        return inventoryRollbackEventOutboxRepository
                .findByStatusAndRetryCountLessThan(CompensationOutboxStatus.FAILED, maxRetryCount)
                .size();
    }

    // 이벤트를 Outbox에 저장 (PENDING 상태로 저장하여 스케줄러가 발행)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToOutbox(String orderCode, String productCode, Integer quantity) {
        try {
            String referenceCode = Generators.timeBasedEpochGenerator().generate().toString();
            InventoryRollbackEventOutbox outbox = InventoryRollbackEventOutbox.from(
                    orderCode, productCode, quantity, referenceCode, objectMapper);
            inventoryRollbackEventOutboxRepository.save(outbox);

            log.debug("재고 롤백 이벤트 Outbox 저장 완료 (PENDING) - orderCode: {}, productCode: {}, quantity: {}, referenceCode: {}, outboxId: {}",
                    orderCode, productCode, quantity, referenceCode, outbox.getId());
        } catch (Exception e) {
            log.error("재고 롤백 이벤트 Outbox 저장 실패 - orderCode: {}, productCode: {}, quantity: {}, error: {}",
                    orderCode, productCode, quantity, e.getMessage(), e);
        }
    }
}

