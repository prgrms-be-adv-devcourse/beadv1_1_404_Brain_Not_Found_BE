package com.ll.payment.model.entity.history;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.payment.model.enums.PaidType;
import com.ll.payment.model.enums.PaymentHistoryActionType;
import com.ll.payment.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_histories")
public class PaymentHistoryEntity extends BaseEntity {

    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private String paymentCode;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String orderCode;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private String buyerCode;

    @Column(nullable = false)
    private Integer paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaidType paidType;

    @Column(nullable = true)
    private String paymentKey; // 토스 결제용 paymentKey

    // 상태 및 이력
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PaymentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus currentStatus;

    @Column(nullable = false)
    private LocalDateTime statusChangedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentHistoryActionType actionType;

    // 외부 연동 정보
//    @Column(nullable = true)
//    private Long depositHistoryId;

    @Column(nullable = true)
    private String externalPaymentId;

    @Column(nullable = true)
    private String externalRefundId;

    // 메타데이터
    @Column(columnDefinition = "TEXT", nullable = true)
    private String reason;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String errorMessage;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String requestData; // JSON 형태로 저장

    @Column(columnDefinition = "TEXT", nullable = true)
    private String responseData; // JSON 형태로 저장

    @Column(nullable = true)
    private String createdBy; // 생성자 (시스템 또는 사용자)
}
