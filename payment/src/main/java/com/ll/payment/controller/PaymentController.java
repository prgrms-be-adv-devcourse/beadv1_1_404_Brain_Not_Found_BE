package com.ll.payment.controller;

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
public class PaymentController { // TODO : 보상 전략

    private final PaymentService paymentService;

    @PostMapping("/toss")
    public ResponseEntity<?> tossPayment(
            @RequestBody PaymentRequest request
            ) {
        paymentService.tossPayment(request);

        return ResponseEntity.ok("Toss payment handled successfully");
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> depositPayment(
            @RequestBody PaymentRequest request
    ) {
        paymentService.depositPayment(request);

        return ResponseEntity.ok("Deposit managed successfully");
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@RequestBody Payment payment) {

        return ResponseEntity.ok("Refund processed successfully");
    }

}
