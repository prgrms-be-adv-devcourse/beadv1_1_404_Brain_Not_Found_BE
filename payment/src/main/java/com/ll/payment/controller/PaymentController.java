package com.ll.payment.controller;

import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.vo.TossPaymentRequest;
import com.ll.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity processPayment(@RequestBody Payment payment) {

        return ResponseEntity.ok("Payment processed successfully");
    }

    @PostMapping("/toss")
    public ResponseEntity tossPayment(@RequestBody Payment payment) {

        return ResponseEntity.ok("Toss payment handled successfully");
    }

    @PostMapping("/deposit")
    public ResponseEntity depositPayment(@RequestBody Payment payment) {

        return ResponseEntity.ok("Deposit managed successfully");
    }

    @PostMapping("/refund")
    public ResponseEntity refundPayment(@RequestBody Payment payment) {

        return ResponseEntity.ok("Refund processed successfully");
    }

    public ResponseEntity<String> confirmPayment(
            @RequestBody TossPaymentRequest request
    ) {
        String response = paymentService.confirmPayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
