package com.ll.payment.payment.service.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.exception.BaseException;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.service.DepositService;
import com.ll.payment.payment.exception.PaymentErrorCode;
import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.entity.PaymentHistoryEntity;
import com.ll.payment.payment.model.enums.PaidType;
import com.ll.payment.payment.model.enums.PaymentHistoryActionType;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.request.PaymentRequest;
import com.ll.payment.payment.model.vo.request.TossPaymentCreateRequest;
import com.ll.payment.payment.model.vo.request.TossPaymentRequest;
import com.ll.payment.payment.model.vo.response.TossPaymentCreateResponse;
import com.ll.payment.payment.model.vo.response.TossPaymentResponse;
import com.ll.payment.payment.repository.PaymentHistoryJpaRepository;
import com.ll.payment.payment.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentServiceImpl implements TossPaymentService {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentHistoryJpaRepository paymentHistoryJpaRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final DepositService depositService;

    @Value("${payment.secretKey}")
    private String secretKey;
    @Value("${payment.targetUrl}")
    private String targetUrl;
    @Value("${payment.createUrl}")
    private String createUrl;
    @Value("${payment.successUrl}")
    private String successUrl;
    @Value("${payment.failUrl}")
    private String failUrl;
    @Value("${payment.useMockPaymentKey:false}")
    private boolean useMockPaymentKey;

    @Override
    @Transactional
    public Payment tossPayment(PaymentRequest request, PaymentStatus finalStatus) {
        // 1. 비관적 락으로 중복 결제 체크 (메서드 시작 부분)
        Optional<Payment> existingCompletedPayment = paymentJpaRepository
                .findByOrderIdAndPaymentStatusWithLock(
                        request.orderId(),
                        PaymentStatus.COMPLETED
                );

        if (existingCompletedPayment.isPresent()) {
            log.warn("이미 결제 완료된 주문입니다. orderId: {}, paymentId: {}",
                    request.orderId(), existingCompletedPayment.get().getId());
            return existingCompletedPayment.get();
        }

        // 2. 진행 중인 결제(PENDING)가 있는지 확인 (락 없이 조회)
        Optional<Payment> existingPendingPayment = paymentJpaRepository
                .findByOrderIdAndPaymentStatus(
                        request.orderId(),
                        PaymentStatus.PENDING
                );

        if (existingPendingPayment.isPresent()) {
            log.warn("이미 진행 중인 결제가 있습니다. orderId: {}, paymentId: {}",
                    request.orderId(), existingPendingPayment.get().getId());
            throw new BaseException(PaymentErrorCode.DUPLICATE_PAYMENT_REQUEST);
        }

        String paymentKey = request.paymentKey();
        if (paymentKey == null || paymentKey.isBlank()) {
            paymentKey = createPayment(
                    request.orderId(),
                    "주문번호: " + request.orderId(),
                    "고객 이름",
                    request.paidAmount()
            );
        }

        // 결제 요청 이력 저장 (락 유지 중)
        PaymentHistoryEntity requestHistory = PaymentHistoryEntity.create(
                null, // paymentId (아직 생성 전)
                PaymentHistoryActionType.REQUEST,
                PaymentStatus.PENDING,
                "TOSS", // pgName
                paymentKey,
                null, // transactionId
                request.paidAmount(),
                null, // failCode
                null, // failMessage
                null, // metadata
                null, // approvedAt
                null  // refundedAt
        );
        paymentHistoryJpaRepository.save(requestHistory);

        // 1) 결제 엔티티 생성
        Payment payment = Payment.createTossPayment(
                request.orderId(),
                request.buyerId(),
                request.paidAmount(),
                paymentKey
        );
        paymentJpaRepository.save(payment);

        // 3) 실제 Toss 승인 요청
        TossPaymentRequest tossRequest = TossPaymentRequest.from(
                paymentKey,
                request.orderCode(),
                request.paidAmount()
        );

        try {
            String response = confirmPayment(tossRequest);
            TossPaymentResponse tossPaymentResponse = parseTossResponse(response);
            validateTossResponse(request, tossPaymentResponse);

            payment.markSuccess(
                    finalStatus,
                    tossPaymentResponse.approvedAt() != null
                            ? tossPaymentResponse.approvedAt().toLocalDateTime()
                            : LocalDateTime.now()
            );
            paymentJpaRepository.save(payment);

            // 결제 성공 이력 저장
            PaymentHistoryEntity successHistory = PaymentHistoryEntity.create(
                    payment.getId(),
                    PaymentHistoryActionType.SUCCESS,
                    finalStatus,
                    "TOSS",
                    paymentKey,
                    tossPaymentResponse.lastTransactionKey(), // transactionId
                    request.paidAmount(),
                    null, // failCode
                    null, // failMessage
                    response, // metadata
                    tossPaymentResponse.approvedAt() != null
                            ? tossPaymentResponse.approvedAt().toLocalDateTime()
                            : LocalDateTime.now(), // approvedAt
                    null  // refundedAt
            );
            paymentHistoryJpaRepository.save(successHistory);

            return payment;
        } catch (Exception e) {
            // 결제 실패 이력 저장
            PaymentHistoryEntity failHistory = PaymentHistoryEntity.create(
                    payment.getId(),
                    PaymentHistoryActionType.FAIL,
                    PaymentStatus.PENDING, // 실패 시 PENDING 상태
                    "TOSS",
                    paymentKey,
                    null, // transactionId
                    request.paidAmount(),
                    null, // failCode
                    e.getMessage(), // failMessage
                    null, // metadata
                    null, // approvedAt
                    null  // refundedAt
            );
            paymentHistoryJpaRepository.save(failHistory);

            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment chargeDepositWithToss(PaymentRequest payment, int chargeAmount) {
        // 토스 결제
        PaymentRequest chargeTossRequest = payment.withAmountAndType(chargeAmount, PaidType.TOSS_PAYMENT);
        Payment chargeTossPayment = tossPayment(chargeTossRequest, PaymentStatus.CHARGE);
        log.debug("충전용 토스 결제 완료 - paymentId: {}, amount: {}",
                chargeTossPayment.getId(), chargeAmount);

        // 토스 결제 성공 후 예치금 충전
        String chargeReferenceCode = "CHARGE-" + payment.orderId() + "-" + System.currentTimeMillis();

        depositService.chargeDeposit(
                payment.buyerCode(),
                new DepositTransactionRequest((long) chargeAmount, chargeReferenceCode)
        );
        log.debug("예치금 충전 완료 - buyerCode: {}, amount: {}",
                payment.buyerCode(), chargeAmount);

        return chargeTossPayment;
    }

    private String createPayment(Long orderId, String orderName, String customerName, Integer amount) {
        if (useMockPaymentKey) {
            String mockPaymentKey = "tgen_test_" + System.currentTimeMillis() + "_" + orderId;
            log.debug("테스트용 더미 paymentKey 생성: {}", mockPaymentKey);
            return mockPaymentKey;
        }

        // 결제 생성 요청(POST /v1/payments) 시 Request Body 에 사용할 수 있는 주요 필드 <- TossPaymentCreateRequest
        TossPaymentCreateRequest createRequest = new TossPaymentCreateRequest(
                amount,
                "ORDER-" + orderId,
                orderName,
                customerName,
                successUrl,
                failUrl
        );

        try {
            log.debug("Toss 결제 생성 요청 - orderId: {}, orderName: {}, amount: {}, successUrl: {}, failUrl: {}",
                    "ORDER-" + orderId, orderName, amount, successUrl, failUrl);

            String response = restClient.post()
                    .uri(createUrl)
                    .headers(headers -> headers.set("Authorization", createAuthorizationHeader()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createRequest)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        try {
                            String errorBody = res.getBody() != null ? new String(res.getBody().readAllBytes()) : "No error body";
                            log.error("Toss 결제 생성 API 에러 - Status: {}, Body: {}", res.getStatusCode(), errorBody);
                            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_CREATE_FAILED);
                        } catch (Exception e) {
                            if (e instanceof BaseException) {
                                throw (BaseException) e;
                            }
                            log.error("에러 응답 읽기 실패 - Status: {}", res.getStatusCode(), e);
                            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_CREATE_FAILED);
                        }
                    })
                    .body(String.class);

            log.debug("Toss 결제 생성 응답: {}", response);
            TossPaymentCreateResponse createResponse = objectMapper.readValue(response, TossPaymentCreateResponse.class);
            return createResponse.paymentKey();
        } catch (Exception e) {
            log.error("토스 결제 생성 중 예외 발생: {}", e.getMessage());
            log.warn("Toss Payments API 호출 실패. 테스트용 더미 paymentKey를 생성합니다.");
            // API 호출 실패 시 더미 paymentKey 생성
            String mockPaymentKey = "tgen_test_" + System.currentTimeMillis() + "_" + orderId;
            log.debug("테스트용 더미 paymentKey 생성: {}", mockPaymentKey);
            return mockPaymentKey;
        }
    }

    private String confirmPayment(TossPaymentRequest request) {
        return restClient.post()
                .uri(targetUrl)
                .headers(headers -> headers.set("Authorization", createAuthorizationHeader()))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    private TossPaymentResponse parseTossResponse(String response) {
        try {
            return objectMapper.readValue(response, TossPaymentResponse.class);
        } catch (Exception e) {
            log.error("토스 결제 응답 파싱에 실패했습니다.", e);
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_RESPONSE_PARSE_FAILED);
        }
    }

    private void validateTossResponse(PaymentRequest request, TossPaymentResponse tossPaymentResponse) {
        if (!"DONE".equalsIgnoreCase(tossPaymentResponse.status())) {
            log.warn("토스 결제 승인 상태가 DONE이 아닙니다. status: {}", tossPaymentResponse.status());
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_STATUS_INVALID);
        }
        if (request.paidAmount() != tossPaymentResponse.totalAmount()) {
            log.warn("토스 승인 금액과 요청 금액이 일치하지 않습니다. 요청: {}, 승인: {}",
                    request.paidAmount(), tossPaymentResponse.totalAmount());
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private String createAuthorizationHeader() {
        String target = secretKey + ":";
        Base64.Encoder encoder = Base64.getEncoder();
        return "Basic " + encoder.encodeToString(target.getBytes(StandardCharsets.UTF_8));
    }
}

