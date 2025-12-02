package com.ll.order.domain.model.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.persistence.BaseEntity;
import com.ll.core.model.vo.kafka.OrderEvent;
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
@Table(name = "order_event_outbox")
public class OrderEventOutbox extends BaseEntity {

    @Column(nullable = false, name = "reference_code")
    private String referenceCode;

    @Column(nullable = false, name = "amount")
    private Long amount;

    @Column(nullable = false, name = "event_payload", columnDefinition = "TEXT")
    private String eventPayload; // OrderEvent를 JSON으로 직렬화한 값

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void incrementRetryCount(String errorMessage) {
        this.retryCount++;
        this.lastErrorMessage = errorMessage;
    }

    public void markAsFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.lastErrorMessage = errorMessage;
    }

    public static OrderEventOutbox from(OrderEvent orderEvent, ObjectMapper objectMapper) {
        try {
            String eventPayload = objectMapper.writeValueAsString(orderEvent);
            return OrderEventOutbox.builder()
                    .referenceCode(orderEvent.referenceCode())
                    .amount(orderEvent.amount())
                    .eventPayload(eventPayload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("OrderEvent를 JSON으로 직렬화하는 중 오류 발생", e);
        }
    }

    public enum OutboxStatus {
        PENDING,    // 발행 대기
        PUBLISHED,  // 발행 완료
        FAILED      // 발행 실패 (재시도 한계 초과)
    }
}

