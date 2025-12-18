package com.ll.payment.payment.service;

import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.PaymentProcessResult;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.payment.model.vo.request.PaymentRequest;
import com.ll.payment.payment.service.deposit.DepositPaymentService;
import com.ll.payment.payment.service.refund.PaymentRefundService;
import com.ll.payment.payment.service.toss.TossPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TossPaymentService tossPaymentService;
    private final PaymentRefundService paymentRefundService;
    private final DepositPaymentService depositPaymentService;

    @Override
    public PaymentProcessResult depositPayment(PaymentRequest payment) {
        // DepositPaymentService에서 예치금 충분/부족 모두 처리
        return depositPaymentService.depositPayment(payment);
    }

    @Override
    public Payment tossPayment(PaymentRequest request, PaymentStatus finalStatus) {
        return tossPaymentService.tossPayment(request, finalStatus);
    }

    @Override
    public Payment refundPayment(PaymentRefundRequest request) {
        return paymentRefundService.refundPayment(request);
    }

    @Override
    public Payment depositChargeWithToss(PaymentRequest request) {
        // 일반 토스 결제 코드 재사용 (CHARGE 상태로 결제)
        Payment payment = tossPaymentService.tossPayment(request, PaymentStatus.CHARGE);
        
        // 결제 성공 후 예치금 충전
        String chargeReferenceCode = "CHARGE-" + (request.orderId() != null ? request.orderId() : "DEPOSIT") + "-" + System.currentTimeMillis();
        
        depositPaymentService.chargeDepositAfterToss(
                request.buyerCode(),
                request.paidAmount(),
                chargeReferenceCode
        );
        
        log.debug("예치금 충전 완료 - buyerCode: {}, amount: {}", request.buyerCode(), request.paidAmount());
        return payment;
    }

}
