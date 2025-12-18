package com.ll.payment.payment.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.payment.payment.model.enums.PaidType;
import com.ll.payment.payment.model.enums.PaymentHistoryActionType;
import com.ll.payment.payment.model.enums.PaymentStatus;
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

    // 기본 식별자
    @Column(nullable = true)
    private Long paymentId;

    // 이벤트 정보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentHistoryActionType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = true)
    private String pgName; // toss, kakaopay 등

    @Column(nullable = true)
    private String paymentKey; // Toss 결제 식별자

    @Column(nullable = true)
    private String transactionId; // PG 내부 트랜잭션 ID (토스: mId / paymentKey / transactionKey 등)

    @Column(nullable = true) // 실패 시에는 null일 수 있음
    private Integer amount; // 이벤트 당시 결제 금액

    @Column(nullable = true)
    private String currency; // KRW, USD 등

    @Column(nullable = true)
    private String failCode; // 실패 코드

    @Column(columnDefinition = "TEXT", nullable = true)
    private String failMessage; // 실패 메시지

    @Column(columnDefinition = "TEXT", nullable = true)
    private String metadata; // 기타 PG 응답 데이터 (JSON 형태)

    @Column(nullable = true)
    private LocalDateTime requestedAt; // PG에 결제 요청 시각

    @Column(nullable = true)
    private LocalDateTime approvedAt; // 결제 승인 시각

    @Column(nullable = true)
    private LocalDateTime refundedAt; // 환불 완료 시각

    public static PaymentHistoryEntity create(
            Long paymentId,
            PaymentHistoryActionType eventType,
            PaymentStatus paymentStatus,
            String pgName,
            String paymentKey,
            String transactionId,
            Integer amount,
            String failCode,
            String failMessage,
            String metadata,
            LocalDateTime approvedAt,
            LocalDateTime refundedAt
    ) {
        PaymentHistoryEntity paymentHistory = new PaymentHistoryEntity();
        paymentHistory.paymentId = paymentId;
        paymentHistory.eventType = eventType;
        paymentHistory.paymentStatus = paymentStatus;
        paymentHistory.pgName = pgName;
        paymentHistory.paymentKey = paymentKey;
        paymentHistory.transactionId = transactionId;
        paymentHistory.amount = amount;
        paymentHistory.currency = "KRW"; // 통화는 항상 KRW로 통일
        paymentHistory.failCode = failCode;
        paymentHistory.failMessage = failMessage;
        paymentHistory.metadata = metadata;
        paymentHistory.requestedAt = LocalDateTime.now(); // 요청 시각은 자동으로 현재 시각으로 설정
        paymentHistory.approvedAt = approvedAt;
        paymentHistory.refundedAt = refundedAt;
        
        return paymentHistory;
    }

    public static PaymentHistoryEntity createRefundRequestHistory(Payment payment, Integer refundAmount) {
        String pgName = payment.getPaidType() == PaidType.TOSS_PAYMENT ? "TOSS" : "DEPOSIT";
        
        return create(
                payment.getId(),
                PaymentHistoryActionType.REFUND_REQUEST,
                PaymentStatus.REFUNDED,
                pgName,
                payment.getPaymentKey(),
                null, // transactionId
                refundAmount,
                null, // failCode
                null, // failMessage
                null, // metadata
                null, // approvedAt
                null  // refundedAt
        );
    }

    public static PaymentHistoryEntity createRefundDoneHistory(Payment payment, Integer refundAmount, String refundResponse) {
        String pgName = payment.getPaidType() == PaidType.TOSS_PAYMENT ? "TOSS" : "DEPOSIT";
        
        return create(
                payment.getId(),
                PaymentHistoryActionType.REFUND_DONE,
                PaymentStatus.REFUNDED,
                pgName,
                payment.getPaymentKey(),
                null, // transactionId
                refundAmount,
                null, // failCode
                null, // failMessage
                refundResponse, // metadata (토스 환불 응답)
                null, // approvedAt
                LocalDateTime.now() // refundedAt
        );
    }

    public static PaymentHistoryEntity createRefundFailHistory(Payment payment, Integer refundAmount, String errorMessage) {
        String pgName = payment.getPaidType() == PaidType.TOSS_PAYMENT ? "TOSS" : "DEPOSIT";
        
        return create(
                payment.getId(),
                PaymentHistoryActionType.FAIL,
                PaymentStatus.REFUNDED,
                pgName,
                payment.getPaymentKey(),
                null, // transactionId
                refundAmount,
                null, // failCode
                errorMessage, // failMessage
                null, // metadata
                null, // approvedAt
                null  // refundedAt
        );
    }
}
