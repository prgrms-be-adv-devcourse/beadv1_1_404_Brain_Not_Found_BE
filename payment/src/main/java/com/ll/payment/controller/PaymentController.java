package com.ll.payment.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaymentStatus;
import com.ll.payment.model.vo.PaymentProcessResult;
import com.ll.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.model.vo.request.PaymentRequest;
import com.ll.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController { // TODO : 결제 실패 시 주문 상태 갱신 또는 트랜잭션 롤백 정책 ( 트랜잭션 관리가 가장 중요 )

    private final PaymentService paymentService;

    @GetMapping("/ping")
    public ResponseEntity<BaseResponse<String>> pong() {
        System.out.println("PaymentController.pong");
        return BaseResponse.ok("Ok");
    }

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
        // TODO 예치금 서비스에서 반환하는 차감 이력 ID를 저장하도록 확장 필요
        return BaseResponse.ok(result);
    }

    @PostMapping("/refund")
    public ResponseEntity<BaseResponse<Payment>> refundPayment(
            @RequestBody PaymentRefundRequest request
    ) {
        Payment result = paymentService.refundPayment(request);
        return BaseResponse.ok(result);
    }

}
