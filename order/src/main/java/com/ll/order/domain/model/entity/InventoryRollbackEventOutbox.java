package com.ll.order.domain.model.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.persistence.BaseEntity;
import com.ll.core.model.vo.kafka.InventoryEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compensation_event_outbox")
public class InventoryRollbackEventOutbox extends BaseEntity {

    @Column(nullable = false, name = "order_code")
    private String orderCode;

    @Column(nullable = false, name = "product_code")
    private String productCode;

    @Column(nullable = false, name = "quantity")
    private Integer quantity;

    @Column(nullable = false, name = "reference_code")
    private String referenceCode;

    @Column(nullable = false, name = "event_payload", columnDefinition = "TEXT")
    private String eventPayload; // InventoryEvent를 JSON으로 직렬화한 값

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompensationOutboxStatus status = CompensationOutboxStatus.PENDING;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public void markAsPublished() {
        this.status = CompensationOutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void incrementRetryCount(String errorMessage) {
        this.retryCount++;
        this.lastErrorMessage = errorMessage;
    }

    public void markAsFailed(String errorMessage) {
        this.status = CompensationOutboxStatus.FAILED;
        this.lastErrorMessage = errorMessage;
    }

    public static InventoryRollbackEventOutbox from(
            String orderCode,
            String productCode,
            Integer quantity,
            String referenceCode,
            ObjectMapper objectMapper
    ) {
        try {
            InventoryEvent inventoryEvent = InventoryEvent.stockRollbackEvent(productCode, quantity, referenceCode);
            String eventPayload = objectMapper.writeValueAsString(inventoryEvent);
            
            return InventoryRollbackEventOutbox.builder()
                    .orderCode(orderCode)
                    .productCode(productCode)
                    .quantity(quantity)
                    .referenceCode(referenceCode)
                    .eventPayload(eventPayload)
                    .status(CompensationOutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("InventoryEvent를 JSON으로 직렬화하는 중 오류 발생", e);
        }
    }

    public enum CompensationOutboxStatus {
        PENDING,    // 발행 대기
        PUBLISHED,  // 발행 완료
        FAILED      // 발행 실패 (재시도 한계 초과)
    }
}

