package com.ll.payment.payment.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.PaymentProcessResult;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.payment.model.vo.request.PaymentRequest;
import com.ll.payment.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/toss")
    public ResponseEntity<BaseResponse<Payment>> tossPayment(
            @RequestBody PaymentRequest request
    ) {
        Payment payment = paymentService.tossPayment(request, PaymentStatus.COMPLETED);
        return BaseResponse.ok(payment);
    }

    @PostMapping("/deposit")
    public ResponseEntity<BaseResponse<PaymentProcessResult>> depositPayment(
            @RequestBody PaymentRequest request
    ) {
        PaymentProcessResult result = paymentService.depositPayment(request);
        return BaseResponse.ok(result);
    }

    @PostMapping("/refund")
    public ResponseEntity<BaseResponse<Payment>> refundPayment(
            @RequestBody PaymentRefundRequest request
    ) {
        Payment result = paymentService.refundPayment(request);
        return BaseResponse.ok(result);
    }

    @PostMapping("/deposit/charge")
    public ResponseEntity<BaseResponse<Payment>> depositChargeWithToss(
            @RequestBody PaymentRequest request
    ) {
        Payment payment = paymentService.depositChargeWithToss(request);
        return BaseResponse.ok(payment);
    }

}
