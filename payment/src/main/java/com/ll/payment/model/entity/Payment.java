package com.ll.payment.model.entity;

import com.ll.payment.model.enums.PaidType;
import com.ll.payment.model.enums.PaymentStatus;
import com.ll.payment.util.PaymentCodeGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int paidAmount;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, unique = true)
    private String paymentCode;

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

    private Payment(int paidAmount,
                    Long buyerId,
                    Long orderId,
                    String paymentCode,
                    PaymentStatus paymentStatus,
                    PaidType paidType,
                    Long depositHistoryId,
                    LocalDateTime paidAt) {
        this.paidAmount = paidAmount;
        this.buyerId = buyerId;
        this.orderId = orderId;
        this.paymentCode = paymentCode;
        this.paymentStatus = paymentStatus;
        this.paidType = paidType;
        this.depositHistoryId = depositHistoryId;
        this.paidAt = paidAt;
    }

    public static Payment createTossPayment(Long orderId, Long buyerId, int paidAmount) {
        return new Payment(
                paidAmount,
                buyerId,
                orderId,
                PaymentCodeGenerator.newPaymentCode(),
                PaymentStatus.PENDING,
                PaidType.TOSS_PAYMENT,
                0L,
                LocalDateTime.now()
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
                PaymentCodeGenerator.newPaymentCode(),
                PaymentStatus.COMPLETED,
                PaidType.DEPOSIT,
                depositHistoryId,
                LocalDateTime.now()
        );
    }

    public void markSuccess(PaymentStatus status, LocalDateTime approvedAt) {
        this.paymentStatus = status;
        this.paidAt = approvedAt;
    }
}
