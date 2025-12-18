package com.ll.payment.payment.model.entity.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.persistence.BaseEntity;
import com.ll.payment.payment.model.enums.PaymentOutboxStatus;
import com.ll.payment.payment.model.vo.PaymentRefundNotificationPayload;
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
@Table(name = "payment_refund_notification_outbox")
public class PaymentRefundNotificationOutbox extends BaseEntity {

    @Column(nullable = false, name = "notification_payload", columnDefinition = "TEXT")
    private String notificationPayload; // 주문 서비스 알림 정보를 JSON으로 직렬화한 값 (orderCode, status 포함)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentOutboxStatus status = PaymentOutboxStatus.PENDING;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 발행 완료 시점 (createdAt와 다름)

    public void markAsPublished() {
        this.status = PaymentOutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void incrementRetryCount(String errorMessage) {
        this.retryCount++;
        this.lastErrorMessage = errorMessage;
    }

    public void markAsFailed(String errorMessage) {
        this.status = PaymentOutboxStatus.FAILED;
        this.lastErrorMessage = errorMessage;
    }

    public static PaymentRefundNotificationOutbox from(String orderCode, String statusValue, ObjectMapper objectMapper) {
        try {
            // 알림 정보를 JSON으로 직렬화
            PaymentRefundNotificationPayload payload = new PaymentRefundNotificationPayload(orderCode, statusValue);
            String notificationPayload = objectMapper.writeValueAsString(payload);
            
            return PaymentRefundNotificationOutbox.builder()
                    .notificationPayload(notificationPayload)
                    .status(PaymentOutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("주문 서비스 알림 정보를 JSON으로 직렬화하는 중 오류 발생", e);
        }
    }
}

