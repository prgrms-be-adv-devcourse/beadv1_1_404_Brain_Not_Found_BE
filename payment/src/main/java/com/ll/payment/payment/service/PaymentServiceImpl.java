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

}
