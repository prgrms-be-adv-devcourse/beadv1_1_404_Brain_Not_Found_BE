package com.ll.payment.controller;

import com.ll.core.model.response.BaseResponse;
import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.vo.PaymentProcessResult;
import com.ll.payment.model.vo.request.PaymentCreateRequest;
import com.ll.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.model.vo.request.PaymentRequest;
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
    /*
     1. 예치금 결제
     - http://localhost:8084/api/payments/deposit (POST)
     - body : {
          "orderId": 1,
          "buyerId": 1,
          "buyerCode": "019a90ab-fcf3-7413-af08-7121cc99378b", <----- 더미 데이터로 만든 실제 사용자 코드 써야함
          "paidAmount": 11,
          "paidType": "DEPOSIT",
          "paymentKey": null
        }
      - 예상 response:
      {
        "status": 200,
        "message": "success",
        "data": {
            "depositPayment": {
                "id": 1,
                "paidAmount": 11,
                "buyerId": 1,
                "orderId": 1,
                "paymentCode": "PAY-019A916A735C70339D4F2FB787CDCA99",
                "depositHistoryId": 0,
                "paidAt": "2025-11-17T19:44:24.0294598",
                "paymentStatus": "COMPLETED",
                "paidType": "DEPOSIT"
            },
            "tossPayment": null
        }
    }

    2. 토스 결제 (postman 두 번 해야함)
    - http://localhost:8084/api/payments/create (POST)
    - body : {
        "orderId": 1,
        "orderName": "테스트상품",
        "customerName": "테스트고객",
        "amount": 10
        }
    - Content-Type : application/json
    - 예상 response:{
            "status": 200,
            "message": "success",
            "data": "tgen_test_1763377899796_1" <----- 이 값을 잘 보세요
        }

    - http://localhost:8084/api/payments/toss (POST)
    - body :
    {
      "orderId": 1,
      "buyerId": 1,
      "buyerCode": "019a90ab-fcf3-7413-af08-7121cc99378b", <----- 더미 데이터로 생성한 실제 사용자 코드 써야함
      "paidAmount": 10,
      "paidType": "TOSS_PAYMENT",
      "paymentKey": "tgen_test_1763377899796_1" <----- 아까 잘 보라고 한 거
    }
    - Content-Type : application/json
    - 예상 response:
    {
        "status": 200,
        "message": "success",
        "data": {
            "id": 1,
            "paidAmount": 10,
            "buyerId": 1,
            "orderId": 1,
            "paymentCode": "PAY-019A91849C6B7898B29FCE6F4EE652F4",
            "depositHistoryId": 0,
            "paidAt": "2025-11-17T20:12:58.651688",
            "paymentStatus": "COMPLETED",
            "paidType": "TOSS_PAYMENT"
        }
    }
     */

    private final PaymentService paymentService;

    @PostMapping("/toss")
    public ResponseEntity<BaseResponse<Payment>> tossPayment(
            @RequestBody PaymentRequest request
    ) {
        Payment payment = paymentService.tossPayment(request);
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

    @PostMapping("/create")
    public ResponseEntity<BaseResponse<String>> createPayment(
            @RequestBody PaymentCreateRequest request
    ) {
        String paymentKey = paymentService.createPayment(
                request.orderId(),
                request.orderName(),
                request.customerName(),
                request.amount()
        );
        return BaseResponse.ok(paymentKey);
    }

}
