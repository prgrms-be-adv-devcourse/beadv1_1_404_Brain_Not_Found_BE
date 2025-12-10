package com.ll.order.domain.model.entity.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.persistence.BaseEntity;
import com.ll.core.model.vo.kafka.RefundEvent;
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
@Table(name = "refund_event_outbox")
public class RefundEventOutbox extends BaseEntity {

    @Column(nullable = false, name = "order_code")
    private String orderCode; // 환불 기준

    @Column(nullable = false, name = "order_item_code")
    private String orderItemCode; // 주문취소로 환불 할 때 재고 변동

    @Column(nullable = false, name = "buyer_code")
    private String buyerCode; // 누구한테 환불하는지

    @Column(nullable = false, name = "reference_code")
    private String referenceCode;

    @Column(nullable = false, name = "amount")
    private Long amount;

    @Column(nullable = false, name = "event_payload", columnDefinition = "TEXT")
    private String eventPayload; // RefundEvent를 JSON으로 직렬화한 값

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

    public static RefundEventOutbox from(RefundEvent refundEvent, String orderCode, ObjectMapper objectMapper) {
        try {
            String eventPayload = objectMapper.writeValueAsString(refundEvent);
            return RefundEventOutbox.builder()
                    .orderCode(orderCode)
                    .orderItemCode(refundEvent.orderItemCode())
                    .buyerCode(refundEvent.buyerCode())
                    .referenceCode(refundEvent.referenceCode())
                    .amount(refundEvent.amount())
                    .eventPayload(eventPayload)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("RefundEvent를 JSON으로 직렬화하는 중 오류 발생", e);
        }
    }
}

