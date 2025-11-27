package com.ll.payment.model.entity;

import com.ll.core.model.persistence.BaseEntity;
import com.ll.payment.model.enums.PaidType;
import com.ll.payment.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int paidAmount;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long depositHistoryId;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaidType paidType;

    @Column(nullable = true)
    private String paymentKey; // 토스 결제용 paymentKey

    private Payment(int paidAmount,
                    Long buyerId,
                    Long orderId,
                    PaymentStatus paymentStatus,
                    PaidType paidType,
                    Long depositHistoryId,
                    LocalDateTime paidAt,
                    String paymentKey) {
        this.paidAmount = paidAmount;
        this.buyerId = buyerId;
        this.orderId = orderId;
        this.paymentStatus = paymentStatus;
        this.paidType = paidType;
        this.depositHistoryId = depositHistoryId;
        this.paidAt = paidAt;
        this.paymentKey = paymentKey;
    }

    public static Payment createTossPayment(Long orderId, Long buyerId, int paidAmount, String paymentKey) {
        return new Payment(
                paidAmount,
                buyerId,
                orderId,
                PaymentStatus.PENDING,
                PaidType.TOSS_PAYMENT,
                0L,
                LocalDateTime.now(),
                paymentKey
        );
    }

    public static Payment createDepositPayment(Long orderId,
                                               Long buyerId,
                                               int paidAmount,
                                               long depositHistoryId) {
        return new Payment(
                paidAmount,
                buyerId,
                orderId,
                PaymentStatus.COMPLETED,
                PaidType.DEPOSIT,
                depositHistoryId,
                LocalDateTime.now(),
                null // 예치금 결제는 paymentKey 없음
        );
    }

    public void markSuccess(PaymentStatus status, LocalDateTime approvedAt) {
        this.paymentStatus = status;
        this.paidAt = approvedAt;
    }

    public void markRefund(LocalDateTime refundedAt) {
        this.paymentStatus = PaymentStatus.REFUNDED;
        this.paidAt = refundedAt;
    }
}
