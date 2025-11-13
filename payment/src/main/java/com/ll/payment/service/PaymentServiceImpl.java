package com.ll.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.payment.client.DepositServiceClient;
import com.ll.payment.model.dto.DepositBalanceResponse;
import com.ll.payment.model.dto.DepositUseRequest;
import com.ll.payment.model.dto.DepositUseResponse;
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
    public void depositPayment(PaymentRequest payment) {
        DepositBalanceResponse balanceResponse = depositServiceClient.getBalance(payment.buyerCode());
        long currentBalance = balanceResponse != null ? balanceResponse.balance() : 0L;
        long requestedAmount = payment.paidAmount();

        if (currentBalance >= requestedAmount) {
            completeDepositPayment(payment, requestedAmount);
            return;
        }

        if (currentBalance > 0) {
            completeDepositPayment(payment, currentBalance);
        }

        long shortageAmount = requestedAmount - currentBalance;
        if (shortageAmount > 0) {
            PaymentRequest tossRequest = new PaymentRequest(
                    payment.orderId(),
                    payment.buyerId(),
                    payment.buyerCode(),
                    (int) shortageAmount,
                    PaidType.TOSS_PAYMENT,
                    payment.paymentKey()
            );
            tossPayment(tossRequest);
        }
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

    private void completeDepositPayment(PaymentRequest payment, long amount) {
        DepositUseResponse useResponse = depositServiceClient.useDeposit(new DepositUseRequest(
                payment.buyerCode(),
                amount,
                payment.orderId(),
                "주문 결제"
        ));

        long depositHistoryId = useResponse != null ? useResponse.historyId() : 0L;
        Payment depositPayment = Payment.createDepositPayment(
                payment.orderId(),
                payment.buyerId(),
                (int) amount,
                depositHistoryId
        );
        paymentJpaRepository.save(depositPayment);
    }
}
