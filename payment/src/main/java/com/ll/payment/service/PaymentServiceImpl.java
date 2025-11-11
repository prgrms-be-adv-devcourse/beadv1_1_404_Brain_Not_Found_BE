package com.ll.payment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.payment.model.dto.TossPaymentResponse;
import com.ll.payment.model.entity.Payment;
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
//    private final DepositRepository depositRepository;

    private final RestClient restClient;
    private final ObjectMapper om;

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
        // 예치금 먼저 확인 (동시성 이슈 고려하면 락 걸어서 가져오도록)
        // Long balance = depositRepository.findByUserId(payment.getBuyerId());

//        if (payment.getPaidAmount() > balance) { // 예치금이 부족한 경우
//            토스 호출 성공 확인
//            true -> 예치금 차감 + 잔액 토스로 결제
//            false -> 예치금 차감 취소 + 오류 메시지 반환
//        } else { // 예치금이 충분한 경우
//             예치금 차감 > 결제 성공 기록 저장 > 주문 도메인에게 결제 완료 반환되도록

        // 1. 사용자 예치금 계좌 조회 (동시성 대비를 위해 PESSIMISTIC_WRITE 잠금 권장)
//        DepositAccount account = depositAccountRepository
//                .findByMemberIdWithLock(payment.getBuyerId())
//                .orElseThrow(() -> new DepositNotFoundException(payment.getBuyerId()));
//
//        long balance = account.getBalance();
//        long amount   = payment.getPaidAmount();
//
//        // 2. 예치금 부족 검증
//        if (balance < amount) {
//            throw new InsufficientDepositException(balance, amount);
//        }
//
//        // 3. 예치금 차감
//        account.decreaseBalance(amount);
//
//        // 4. 결제 엔티티 저장 (예치금 결제 완료 상태)
//        payment.markSuccess(PaidType.DEPOSIT, amount);
//        Payment savedPayment = paymentJpaRepository.save(payment);
//
//        // 5. 예치금 사용 이력 기록
//        DepositHistory history = DepositHistory.use(
//                account,
//                amount,
//                "주문 결제",
//                payment.getOrderCode()
//        );
//        paymentHistoryRepository.save(history);
//
//        // 6. 결과 DTO 반환 (주문 도메인이 사용할 정보만 추려서)
//        return PaymentRe
//        }

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
}
