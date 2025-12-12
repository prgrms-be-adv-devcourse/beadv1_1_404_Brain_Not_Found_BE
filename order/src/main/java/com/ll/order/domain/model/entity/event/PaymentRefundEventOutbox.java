package com.ll.order.domain.model.entity.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.persistence.BaseEntity;
import com.ll.order.domain.model.vo.PaymentRefundRequestEvent;
import com.ll.order.domain.model.enums.order.OutboxStatus;
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
@Table(name = "payment_refund_event_outbox")
public class PaymentRefundEventOutbox extends BaseEntity {

    @Column(nullable = false, name = "order_code")
    private String orderCode;

    @Column(nullable = false, name = "buyer_code")
    private String buyerCode;

    @Column(nullable = false, name = "refund_amount")
    private Integer refundAmount;

    @Column(name = "reason")
    private String reason;

    @Column(nullable = false, name = "event_payload", columnDefinition = "TEXT")
    private String eventPayload; // PaymentRefundRequestEvent를 JSON으로 직렬화한 값

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

    public static PaymentRefundEventOutbox from(PaymentRefundRequestEvent event, String orderCode, ObjectMapper objectMapper) {
        try {
            String eventPayload = objectMapper.writeValueAsString(event);
            return PaymentRefundEventOutbox.builder()
                    .orderCode(orderCode)
                    .buyerCode(event.buyerCode())
                    .refundAmount(event.refundAmount())
                    .reason(event.reason())
                    .eventPayload(eventPayload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("PaymentRefundRequestEvent를 JSON으로 직렬화하는 중 오류 발생", e);
        }
    }
}

