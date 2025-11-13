package com.ll.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.payment.client.DepositServiceClient;
import com.ll.payment.model.dto.DepositInfoResponse;
import com.ll.payment.model.dto.PaymentProcessResult;
import com.ll.payment.model.dto.TossPaymentResponse;
import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaidType;
import com.ll.payment.model.enums.PaymentStatus;
import com.ll.payment.model.vo.PaymentRequest;
import com.ll.payment.model.vo.TossPaymentRequest;
import com.ll.payment.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements  PaymentService {

    private final PaymentJpaRepository paymentJpaRepository;

    private final RestClient restClient;
    private final ObjectMapper om;
    private final DepositServiceClient depositServiceClient;

    @Value("${payment.secretKey}")
    private String secretKey;
    @Value("${payment.targetUrl}")
    private String targetUrl;

    @Override
    public String confirmPayment(TossPaymentRequest request) {
        // api 결제 승인 요청
        String target = secretKey + ":";
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedSecretKey = "Basic " + encoder.encodeToString(target.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> requestMap = om.convertValue(request, new TypeReference<>() {
        });

        return restClient.post()
                .uri(targetUrl)
                .headers(headers -> headers.set("Authorization", encryptedSecretKey))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestMap)
                .retrieve()
                .body(String.class);
    }

    @Override
    public PaymentProcessResult depositPayment(PaymentRequest payment) {
        DepositInfoResponse depositInfo = depositServiceClient.getDepositInfo(payment.buyerCode());
        long currentBalance = depositInfo != null && depositInfo.balance() != null ? depositInfo.balance() : 0L;
        long requestedAmount = payment.paidAmount();

        if (currentBalance >= requestedAmount) {
            Payment depositPayment = completeDepositPayment(payment, requestedAmount);
            return new PaymentProcessResult(depositPayment, null);
        }

        Payment depositPayment = null;
        if (currentBalance > 0) {
            depositPayment = completeDepositPayment(payment, currentBalance);
        }

        long shortageAmount = requestedAmount - currentBalance;
        Payment tossPayment = null;
        if (shortageAmount > 0) {
            PaymentRequest tossRequest = new PaymentRequest(
                    payment.orderId(),
                    payment.buyerId(),
                    payment.buyerCode(),
                    (int) shortageAmount,
                    PaidType.TOSS_PAYMENT,
                    payment.paymentKey()
            );
            tossPayment = tossPayment(tossRequest);
        }

        return new PaymentProcessResult(depositPayment, tossPayment);
    }

    @Override
    public Payment tossPayment(PaymentRequest request) {
        // 1) 결제 엔티티 초안 저장 (상태: PENDING)
        Payment payment = Payment.createTossPayment(
                request.orderId(),
                request.buyerId(),
                request.paidAmount()
        );
        paymentJpaRepository.save(payment);

        // 2) 토스 승인 요청
        TossPaymentRequest tossRequest = new TossPaymentRequest(
                request.paymentKey(),
                payment.getPaymentCode(),
                request.paidAmount()
        );
        String response = confirmPayment(tossRequest);
        TossPaymentResponse tossPaymentResponse = parseTossResponse(response);
        validateTossResponse(request, tossPaymentResponse);

        payment.markSuccess(
                PaymentStatus.COMPLETED,
                tossPaymentResponse.approvedAt()
        );
        paymentJpaRepository.save(payment);

        return payment;
    }

    @Override
    public Payment refundPayment(Payment payment) {
        // TODO 환불 절차
        // 1) paymentCode / orderId 등 식별자로 기존 결제 내역을 조회하고 환불 가능 여부를 검증한다.
        // 2) 결제 수단(PaidType)에 따라 외부 시스템(예: 예치금 입금, 토스 취소 API)에 환불을 요청한다.
        // 3) 환불 성공 시 Payment 상태를 REFUNDED로 갱신하고 환불 일시·금액·외부 환불 키 등을 저장한다.
        // 4) 환불 결과에 맞춰 주문 상태도 업데이트하거나 후속 도메인 이벤트를 발행한다.
        return payment;
    }

    private TossPaymentResponse parseTossResponse(String response) {
        try {
            return om.readValue(response, TossPaymentResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("토스 결제 응답 파싱에 실패했습니다.", e);
        }
    }

    private void validateTossResponse(PaymentRequest request, TossPaymentResponse tossPaymentResponse) {
        if (!"DONE".equalsIgnoreCase(tossPaymentResponse.status())) {
            throw new IllegalStateException("토스 결제 승인 상태가 DONE이 아닙니다. status=" + tossPaymentResponse.status());
        }
        if (request.paidAmount() != tossPaymentResponse.approvedAmount()) {
            throw new IllegalStateException("토스 승인 금액과 요청 금액이 일치하지 않습니다.");
        }
    }

    private Payment completeDepositPayment(PaymentRequest payment, long amount) {
        depositServiceClient.withdraw(payment.buyerCode(), amount, createReferenceCode(payment));
        Payment depositPayment = Payment.createDepositPayment(
                payment.orderId(),
                payment.buyerId(),
                (int) amount,
                0L
        );
        paymentJpaRepository.save(depositPayment);
        return depositPayment;
    }

    private String createReferenceCode(PaymentRequest payment) {
        return "ORDER-" + payment.orderId() + "-" + System.currentTimeMillis();
    }
}
