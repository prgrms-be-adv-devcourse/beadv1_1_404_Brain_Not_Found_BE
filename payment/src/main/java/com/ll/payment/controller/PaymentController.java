package com.ll.payment.controller;

import com.example.core.model.response.BaseResponse;
import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.vo.PaymentRequest;
import com.ll.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController { // TODO : 결제 실패 시 주문 상태 갱신 또는 트랜잭션 롤백 정책 ( 트랜잭션 관리가 가장 중요 )

    private final PaymentService paymentService;

    @PostMapping("/toss")
    public ResponseEntity<BaseResponse<String>> tossPayment(
            @RequestBody PaymentRequest request
            ) {
        paymentService.tossPayment(request);

        return BaseResponse.ok("Toss payment handled successfully");
    }

    @PostMapping("/deposit")
    public ResponseEntity<BaseResponse<String>> depositPayment(
            @RequestBody PaymentRequest request
    ) {
        paymentService.depositPayment(request);

        return BaseResponse.ok("Deposit managed successfully");
    }

    @PostMapping("/refund")
    public ResponseEntity<BaseResponse<String>> refundPayment(@RequestBody Payment payment) {
        return BaseResponse.ok("Refund processed successfully");
    }

}
