package com.ll.payment.service;

import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaymentStatus;
import com.ll.payment.model.vo.PaymentProcessResult;
import com.ll.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.model.vo.request.PaymentRequest;

public interface PaymentService {
    PaymentProcessResult depositPayment(PaymentRequest payment);

    Payment tossPayment(PaymentRequest request, PaymentStatus finalStatus);

    Payment refundPayment(PaymentRefundRequest request);

    /**
     * 예치금 결제 처리
     * 
     * @param payment 결제 요청
     * @param amount 결제 금액
     * @return 예치금 결제 엔티티
     */
    Payment completeDepositPayment(PaymentRequest payment, int amount);

    /**
     * 토스 결제로 예치금 충전
     * 
     * @param payment 원본 결제 요청
     * @param chargeAmount 충전할 금액
     * @return 충전용 토스 결제 엔티티
     */
    Payment chargeDepositWithToss(PaymentRequest payment, int chargeAmount);

    /**
     * 예치금 부족 시 토스 결제로 예치금 충전 후 예치금 결제 처리
     * 
     * @param payment 원본 결제 요청
     * @param shortageAmount 부족한 금액 (토스 결제로 충전할 금액)
     * @param requestedAmount 전체 요청 금액
     * @return 충전용 토스 결제 엔티티 (실패 시 null)
     */
    Payment processDepositPaymentWithCharge(PaymentRequest payment, int shortageAmount, int requestedAmount);
}
