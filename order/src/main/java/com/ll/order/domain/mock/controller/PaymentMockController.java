package com.ll.order.domain.mock.controller;

import com.ll.order.domain.model.vo.request.OrderPaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Payment Service Mock Controller
 * 로컬 개발 환경에서 결제 정보를 모킹하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentMockController {

    /**
     * 예치금 결제 Mock API
     * POST /api/payments/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<String> requestDepositPayment(
            @RequestBody OrderPaymentRequest request
    ) {
        log.info("Mock Payment Service - 예치금 결제 요청: orderCode={}, amount={}", 
                request.orderCode(), request.paidAmount());
        
        // 항상 성공으로 처리
        log.info("Mock Payment Service - 예치금 결제 성공: orderCode={}", request.orderCode());
        return ResponseEntity.ok("결제 완료");
    }

    /**
     * 토스 결제 Mock API
     * POST /api/payments/toss
     */
    @PostMapping("/toss")
    public ResponseEntity<String> requestTossPayment(
            @RequestBody OrderPaymentRequest request
    ) {
        log.info("Mock Payment Service - 토스 결제 요청: orderCode={}, amount={}, paymentKey={}", 
                request.orderCode(), request.paidAmount(), request.paymentKey());
        
        // 항상 성공으로 처리
        log.info("Mock Payment Service - 토스 결제 성공: orderCode={}", request.orderCode());
        return ResponseEntity.ok("결제 완료");
    }

    /**
     * 환불 Mock API
     * POST /api/payments/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<String> requestRefund(
            @RequestBody Map<String, Object> request
    ) {
        String orderCode = (String) request.get("orderCode");
        Integer refundAmount = (Integer) request.get("refundAmount");
        String reason = (String) request.get("reason");
        
        log.info("Mock Payment Service - 환불 요청: orderCode={}, refundAmount={}, reason={}", 
                orderCode, refundAmount, reason);
        
        // 항상 성공으로 처리
        log.info("Mock Payment Service - 환불 성공: orderCode={}", orderCode);
        return ResponseEntity.ok("환불 완료");
    }
}

