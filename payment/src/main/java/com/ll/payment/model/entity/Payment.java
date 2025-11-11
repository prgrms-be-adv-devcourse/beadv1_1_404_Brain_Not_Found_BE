package com.ll.payment.model.entity;

import com.ll.payment.model.enums.PaidType;
import com.ll.payment.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
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

    @Builder
    public Payment(int paidAmount, Long buyerId, Long orderId, String paymentCode, PaymentStatus paymentStatus, PaidType paidType, Long depositHistoryId, LocalDateTime paidAt) {
        this.paidAmount = paidAmount;
        this.buyerId = buyerId;
        this.orderId = orderId;
        this.paymentCode = paymentCode;
        this.paymentStatus = paymentStatus;
        this.paidType = paidType;
        this.depositHistoryId = depositHistoryId;
        this.paidAt = paidAt;
    }
}
