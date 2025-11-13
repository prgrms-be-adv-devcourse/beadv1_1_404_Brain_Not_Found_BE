package com.ll.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.payment.client.DepositServiceClient;
import com.ll.payment.client.OrderServiceClient;
import com.ll.payment.model.dto.DepositInfoResponse;
import com.ll.payment.model.dto.PaymentProcessResult;
import com.ll.payment.model.dto.TossPaymentResponse;
import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaidType;
import com.ll.payment.model.enums.PaymentStatus;
import com.ll.payment.model.vo.PaymentRefundRequest;
import com.ll.payment.model.vo.PaymentRequest;
import com.ll.payment.model.vo.TossPaymentRequest;
import com.ll.payment.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentJpaRepository paymentJpaRepository;

    private final RestClient restClient;
    private final ObjectMapper om;
    private final DepositServiceClient depositServiceClient;
    private final OrderServiceClient orderServiceClient;

    @Value("${payment.secretKey}")
    private String secretKey;
    @Value("${payment.targetUrl}")
    private String targetUrl;

    @Override
    public String confirmPayment(TossPaymentRequest request) {
        return restClient.post()
                .uri(targetUrl)
                .headers(headers -> headers.set("Authorization", createAuthorizationHeader()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    @Override
    public PaymentProcessResult depositPayment(PaymentRequest payment) {
        int currentBalance = Optional.ofNullable(depositServiceClient.getDepositInfo(payment.buyerCode()))
                .map(DepositInfoResponse::balance)
                .orElse(0);
        int requestedAmount = payment.paidAmount();

        if (currentBalance >= requestedAmount) {
            Payment depositPayment = completeDepositPayment(payment, requestedAmount);
            return new PaymentProcessResult(depositPayment, null);
        }

        Payment depositPayment = currentBalance > 0
                ? completeDepositPayment(payment, currentBalance)
                : null;

        int shortageAmount = requestedAmount - currentBalance;
        Payment tossPayment = null;
        if (shortageAmount > 0) {
            PaymentRequest tossRequest = new PaymentRequest(
                    payment.orderId(),
                    payment.buyerId(),
                    payment.buyerCode(),
                    shortageAmount,
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
    public Payment refundPayment(PaymentRefundRequest request) {
        // 환불 절차
        // 1) paymentCode / orderId 등 식별자로 기존 결제 내역을 조회하고 환불 가능 여부를 검증한다.
        // 2) 결제 수단(PaidType)에 따라 외부 시스템(예: 예치금 입금, 토스 취소 API)에 환불을 요청한다.
        // 3) 환불 성공 시 Payment 상태를 REFUNDED로 갱신하고 환불 일시·금액·외부 환불 키 등을 저장한다.
        // 4) 환불 결과에 맞춰 주문 상태도 업데이트하거나 후속 도메인 이벤트를 발행한다.
        // TODO(toss-integration): 실제 토스 취소 API 스펙에 맞춰 요청/응답 필드와 예외 처리를 구체화하세요.
        Payment payment = findPaymentForRefund(request);
        int refundAmount = validateRefundEligibility(payment, request);

        switch (payment.getPaidType()) {
            case DEPOSIT -> processDepositRefund(payment, request, refundAmount);
            case TOSS_PAYMENT -> processTossRefund(payment, request, refundAmount);
            default -> throw new IllegalArgumentException("지원하지 않는 결제 수단입니다: " + payment.getPaidType());
        }

        payment.markRefund(LocalDateTime.now());
        paymentJpaRepository.save(payment);
        notifyOrderRefund(request.orderCode());
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

    private Payment completeDepositPayment(PaymentRequest payment, int amount) {
        depositServiceClient.withdraw(payment.buyerCode(), amount, createReferenceCode(payment.orderId()));
        Payment depositPayment = Payment.createDepositPayment(
                payment.orderId(),
                payment.buyerId(),
                amount,
                0L
        );
        paymentJpaRepository.save(depositPayment);
        return depositPayment;
    }

    private Payment findPaymentForRefund(PaymentRefundRequest request) {
        if (request.paymentId() != null) {
            return paymentJpaRepository.findById(request.paymentId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + request.paymentId()));
        }
        if (request.paymentCode() != null && !request.paymentCode().isBlank()) {
            return paymentJpaRepository.findByPaymentCode(request.paymentCode())
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + request.paymentCode()));
        }
        throw new IllegalArgumentException("환불 대상 결제 정보를 찾을 수 없습니다.");
    }

    private int validateRefundEligibility(Payment payment, PaymentRefundRequest request) {
        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("환불은 완료된 결제만 가능합니다. status=" + payment.getPaymentStatus());
        }

        if (request.orderId() != null && !request.orderId().equals(payment.getOrderId())) {
            throw new IllegalArgumentException("주문 번호가 결제 정보와 일치하지 않습니다.");
        }
        if (request.orderCode() == null || request.orderCode().isBlank()) {
            throw new IllegalArgumentException("환불에는 orderCode가 필요합니다.");
        }

        if (request.paidType() != null && request.paidType() != payment.getPaidType()) {
            throw new IllegalArgumentException("요청한 결제 수단이 실제 결제 수단과 다릅니다.");
        }

        int refundAmount = request.refundAmount() != null ? request.refundAmount() : payment.getPaidAmount();
        if (refundAmount <= 0) {
            throw new IllegalArgumentException("환불 금액이 0 이하입니다.");
        }
        if (refundAmount > payment.getPaidAmount()) {
            throw new IllegalArgumentException("환불 금액이 결제 금액을 초과합니다.");
        }
        if (refundAmount != payment.getPaidAmount()) {
            throw new IllegalArgumentException("부분 환불은 현재 지원되지 않습니다.");
        }

        return refundAmount;
    }

    private void processDepositRefund(Payment payment, PaymentRefundRequest request, int refundAmount) {
        String buyerCode = request.buyerCode();
        if (buyerCode == null || buyerCode.isBlank()) {
            throw new IllegalArgumentException("예치금 환불에는 buyerCode가 필요합니다.");
        }
        depositServiceClient.deposit(buyerCode, refundAmount, createReferenceCode(payment.getOrderId()));
    }

    private void processTossRefund(Payment payment, PaymentRefundRequest request, int refundAmount) {
        String paymentKey = request.paymentKey();
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new IllegalArgumentException("토스 환불에는 paymentKey가 필요합니다.");
        }

        Map<String, Object> cancelRequest = new HashMap<>();
        cancelRequest.put("paymentKey", paymentKey);
        cancelRequest.put("cancelAmount", refundAmount);
        cancelRequest.put("cancelReason", request.reason() != null && !request.reason().isBlank()
                ? request.reason()
                : "USER_REFUND");

        try {
            restClient.post()
                    .uri(targetUrl + "/cancel")
                    .headers(headers -> headers.set("Authorization", createAuthorizationHeader()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cancelRequest)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new IllegalStateException("토스 결제 환불 요청에 실패했습니다.", e);
        }
    }

    private void notifyOrderRefund(String orderCode) {
        try {
            orderServiceClient.updateOrderStatus(orderCode, "REFUNDED");
        } catch (Exception e) {
            throw new IllegalStateException("주문 서비스에 환불 상태를 전달하는 데 실패했습니다.", e);
        }
    }

    private String createReferenceCode(Long orderId) {
        return "ORDER-" + orderId + "-" + System.currentTimeMillis();
    }

    private String createAuthorizationHeader() {
        String target = secretKey + ":";
        Base64.Encoder encoder = Base64.getEncoder();
        return "Basic " + encoder.encodeToString(target.getBytes(StandardCharsets.UTF_8));
    }
}
