package com.ll.payment.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.exception.BaseException;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.model.vo.response.DepositResponse;
import com.ll.payment.deposit.service.DepositService;
import com.ll.payment.global.client.OrderServiceClient;
import com.ll.payment.payment.exception.PaymentErrorCode;
import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.entity.history.PaymentHistoryEntity;
import com.ll.payment.payment.model.enums.PaidType;
import com.ll.payment.payment.model.enums.PaymentHistoryActionType;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.PaymentProcessResult;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final DepositService depositService;

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentHistoryJpaRepository paymentHistoryJpaRepository;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OrderServiceClient orderServiceClient;
    private final PaymentValidator paymentValidator;

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
    public PaymentProcessResult depositPayment(PaymentRequest payment) {
        DepositResponse depositInfo = depositService.getDepositByUserCode(payment.buyerCode());

        int currentBalance = depositInfo.balance().intValue();
        int requestedAmount = payment.paidAmount();

        // 예치금이 충분한 경우 바로 예치금 결제
        if (currentBalance >= requestedAmount) {
            // 결제 요청 이력 저장
            PaymentHistoryEntity requestHistory = PaymentHistoryEntity.create(
                    null, // paymentId (아직 생성 전)
                    PaymentHistoryActionType.REQUEST,
                    PaymentStatus.PENDING,
                    "DEPOSIT", // pgName
                    null, // paymentKey
                    null, // transactionId
                    requestedAmount,
                    null, // failCode
                    null, // failMessage
                    null, // metadata
                    null, // approvedAt
                    null  // refundedAt
            );
            paymentHistoryJpaRepository.save(requestHistory);

            Payment depositPayment = completeDepositPayment(payment, requestedAmount);
            log.debug("예치금 결제 완료 - orderId: {}, amount: {}", payment.orderId(), requestedAmount);
            return new PaymentProcessResult(depositPayment, null);
        }

        // 예치금이 부족한 경우 : 토스 결제로 금액 충전 + 예치금으로 전체 결제
        int shortageAmount = requestedAmount - currentBalance;
        log.debug("예치금 부족 - 현재 잔액: {}, 요청 금액: {}, 부족 금액: {}", 
                currentBalance, requestedAmount, shortageAmount);
        // 복합 결제 처리 (토스 충전 + 예치금 결제)
        Payment chargeTossPayment = processDepositPaymentWithCharge(payment, shortageAmount, requestedAmount);
        Payment depositPayment = completeDepositPayment(payment, requestedAmount);
        log.debug("예치금 결제 완료 - orderId: {}, amount: {}", payment.orderId(), requestedAmount);

        return new PaymentProcessResult(depositPayment, chargeTossPayment);
    }

//     예치금 부족 시 토스 결제로 예치금 충전 후 예치금 결제 처리
    // 보상 로직(환불)을 포함하므로 독립 트랜잭션이 필요함
    // REQUIRES_NEW로 설정하여 실패 시에도 다른 트랜잭션에 영향을 주지 않음
    @Override
    @Transactional
    public Payment processDepositPaymentWithCharge(PaymentRequest payment, int shortageAmount, int requestedAmount) {
        Payment chargeTossPayment = null;
        try {
            // 토스 결제로 예치금 충전
            chargeTossPayment = chargeDepositWithToss(payment, shortageAmount);
            log.debug("복합 결제 처리 완료 - 토스 충전: {}, 예치금 결제 대기: {}", 
                    shortageAmount, requestedAmount);
        } catch (Exception e) {
            log.error("복합 결제 처리 실패 - orderId: {}, shortageAmount: {}, error: {}", 
                    payment.orderId(), shortageAmount, e.getMessage(), e);
            
            // 토스 결제는 성공했지만 예치금 충전 실패한 경우 토스 환불
            if (chargeTossPayment != null && chargeTossPayment.getPaymentStatus() == PaymentStatus.CHARGE) {
                try {
                    processTossRefundForCharge(chargeTossPayment, shortageAmount);
                    log.debug("예치금 충전 실패로 인한 토스 결제 환불 완료 - paymentId: {}", chargeTossPayment.getId());
                } catch (Exception refundException) {
                    log.error("토스 결제 환불 실패 - paymentId: {}, error: {}", 
                            chargeTossPayment.getId(), refundException.getMessage(), refundException);
                    // 환불도 실패한 경우는 수동 처리 필요
                }
            }
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_CREATE_FAILED);
        }
        return chargeTossPayment;
    }

//     토스 결제로 예치금 충전
    // tossPayment가 이미 @Transactional을 가지고 있고, 예치금 충전은 외부 서비스 호출이므로 이 메서드에는 트랜잭션이 필요 없음 (중첩 트랜잭션 방지)
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

    // 환불 절차
    // 1) paymentCode / orderId 등 식별자로 기존 결제 내역을 조회하고 환불 가능 여부를 검증한다.
    // 2) 결제 수단(PaidType)에 따라 외부 시스템(예: 예치금 입금, 토스 취소 API)에 환불을 요청한다.
    // 3) 환불 성공 시 Payment 상태를 REFUNDED로 갱신하고 환불 일시·금액·외부 환불 키 등을 저장한다.
    // 4) 환불 결과에 맞춰 주문 상태도 업데이트하거나 후속 도메인 이벤트를 발행한다.
    // TODO(toss-integration): 실제 토스 취소 API 스펙에 맞춰 요청/응답 필드와 예외 처리를 구체화하세요.
    @Override
    @Transactional
    public Payment refundPayment(PaymentRefundRequest request) {
        Payment payment = findPaymentForRefund(request);
        int refundAmount = paymentValidator.validateRefundEligibility(payment, request);

        // 환불 요청 이력 저장
        PaymentHistoryEntity refundRequestHistory = PaymentHistoryEntity.create(
                payment.getId(),
                PaymentHistoryActionType.REFUND_REQUEST,
                PaymentStatus.REFUNDED,
                payment.getPaidType() == PaidType.TOSS_PAYMENT ? "TOSS" : "DEPOSIT",
                payment.getPaymentKey(),
                null, // transactionId
                refundAmount,
                null, // failCode
                null, // failMessage
                null, // metadata
                null, // approvedAt
                null  // refundedAt
        );
        paymentHistoryJpaRepository.save(refundRequestHistory);

        try {
            String refundResponse = null;
            switch (payment.getPaidType()) {
                case DEPOSIT -> processDepositRefund(payment, request, refundAmount);
                case TOSS_PAYMENT -> refundResponse = processTossRefund(payment, request, refundAmount);
                default -> {
                    log.warn("지원하지 않는 결제 수단입니다. paidType: {}", payment.getPaidType());
                    throw new BaseException(PaymentErrorCode.UNSUPPORTED_PAYMENT_TYPE);
                }
            }

            payment.markRefund(LocalDateTime.now());
            paymentJpaRepository.save(payment);

            // 환불 완료 이력 저장
            PaymentHistoryEntity refundDoneHistory = PaymentHistoryEntity.create(
                    payment.getId(),
                    PaymentHistoryActionType.REFUND_DONE,
                    PaymentStatus.REFUNDED,
                    payment.getPaidType() == PaidType.TOSS_PAYMENT ? "TOSS" : "DEPOSIT",
                    payment.getPaymentKey(),
                    null, // transactionId
                    refundAmount,
                    null, // failCode
                    null, // failMessage
                    refundResponse, // metadata (토스 환불 응답)
                    null, // approvedAt
                    LocalDateTime.now() // refundedAt
            );
            paymentHistoryJpaRepository.save(refundDoneHistory);

            notifyOrderRefund(request.orderCode());
            return payment;
        } catch (Exception e) {
            // 환불 실패 이력 저장
            PaymentHistoryEntity refundFailHistory = PaymentHistoryEntity.create(
                    payment.getId(),
                    PaymentHistoryActionType.FAIL,
                    PaymentStatus.REFUNDED,
                    payment.getPaidType() == PaidType.TOSS_PAYMENT ? "TOSS" : "DEPOSIT",
                    payment.getPaymentKey(),
                    null, // transactionId
                    refundAmount,
                    null, // failCode
                    e.getMessage(), // failMessage
                    null, // metadata
                    null, // approvedAt
                    null  // refundedAt
            );
            paymentHistoryJpaRepository.save(refundFailHistory);

            throw e;
        }
    }
    private TossPaymentResponse parseTossResponse(String response) {
        try {
            return objectMapper.readValue(response, TossPaymentResponse.class);
        } catch (Exception e) {
            log.error("토스 결제 응답 파싱에 실패했습니다.", e);
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_RESPONSE_PARSE_FAILED);
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

    @Override
    @Transactional
    public Payment completeDepositPayment(PaymentRequest payment, int amount) {
        // 1. 비관적 락으로 중복 결제 체크
        Optional<Payment> existingPayment = paymentJpaRepository
                .findByOrderIdAndPaymentStatusWithLock(
                        payment.orderId(),
                        PaymentStatus.COMPLETED
                );
        
        if (existingPayment.isPresent()) {
            log.warn("이미 결제 완료된 주문입니다. orderId: {}, paymentId: {}",
                    payment.orderId(), existingPayment.get().getId());
            return existingPayment.get();
        }
        
        // 2. 예치금 차감 (락 유지 중)
        depositService.withdrawDeposit(
                payment.buyerCode(),
                new DepositTransactionRequest((long) amount, createReferenceCode(payment.orderId())
                ));

        // 3. Payment 엔티티 생성 및 저장 (락 유지 중)
        Payment depositPayment = Payment.createDepositPayment(
                payment.orderId(),
                payment.buyerId(),
                amount,
                0L
        );
        paymentJpaRepository.save(depositPayment);

        // 4. 결제 성공 이력 저장 (락 유지 중)
        PaymentHistoryEntity paymentHistory = PaymentHistoryEntity.create(
                depositPayment.getId(),
                PaymentHistoryActionType.SUCCESS,
                PaymentStatus.COMPLETED,
                "DEPOSIT", // pgName
                null, // paymentKey (예치금 결제는 없음)
                null, // transactionId
                amount,
                null, // failCode
                null, // failMessage
                null, // metadata
                LocalDateTime.now(), // approvedAt
                null  // refundedAt
        );
        paymentHistoryJpaRepository.save(paymentHistory);

        return depositPayment;
    }

    private Payment findPaymentForRefund(PaymentRefundRequest request) {
        if (request.orderId() != null) {
            return paymentJpaRepository.findByOrderIdAndPaymentStatus(
                            request.orderId(), 
                            PaymentStatus.COMPLETED
                    )
                    .orElseThrow(() -> {
                        log.warn("환불 대상 결제 정보를 찾을 수 없습니다. orderId: {}, status: COMPLETED", request.orderId());
                        return new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                    });
        }

        log.warn("환불 대상 결제 정보를 찾을 수 없습니다. orderId, paymentId, paymentCode 모두 없습니다.");
        throw new BaseException(PaymentErrorCode.REFUND_TARGET_NOT_FOUND);
    }

    private void processDepositRefund(Payment payment, PaymentRefundRequest request, int refundAmount) {
        String buyerCode = request.buyerCode();
        if (buyerCode == null || buyerCode.isBlank()) {
            log.warn("예치금 환불에는 buyerCode가 필요합니다.");
            throw new BaseException(PaymentErrorCode.BUYER_CODE_REQUIRED);
        }
        depositService.chargeDeposit(
                buyerCode,
                new DepositTransactionRequest((long) refundAmount, createReferenceCode(payment.getOrderId()))
        );
    }

//     충전 실패 시 토스 결제 환불용 메서드 (PaymentRefundRequest 없이 호출)
    private void processTossRefundForCharge(Payment payment, int refundAmount) {
        String paymentKey = payment.getPaymentKey();
        if (paymentKey == null || paymentKey.isBlank()) {
            log.warn("토스 환불에는 paymentKey가 필요합니다. paymentId: {}, orderId: {}", 
                    payment.getId(), payment.getOrderId());
            throw new BaseException(PaymentErrorCode.PAYMENT_KEY_REQUIRED);
        }

        Map<String, Object> cancelRequest = new HashMap<>();
        cancelRequest.put("paymentKey", paymentKey);
        cancelRequest.put("cancelAmount", refundAmount);
        cancelRequest.put("cancelReason", "예치금 충전 실패로 인한 환불");

        try {
            restClient.post()
                    .uri(targetUrl + "/cancel")
                    .headers(headers -> headers.set("Authorization", createAuthorizationHeader()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cancelRequest)
                    .retrieve()
                    .toBodilessEntity();
            log.debug("충전 실패로 인한 토스 결제 환불 성공 - paymentKey: {}, refundAmount: {}", 
                    paymentKey, refundAmount);
        } catch (Exception e) {
            log.error("충전 실패로 인한 토스 결제 환불 요청에 실패했습니다. paymentKey: {}, refundAmount: {}", 
                    paymentKey, refundAmount, e);
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_REFUND_FAILED);
        }
    }

    private String processTossRefund(Payment payment, PaymentRefundRequest request, int refundAmount) {
        String paymentKey = payment.getPaymentKey();
        if (paymentKey == null || paymentKey.isBlank()) {
            log.warn("토스 환불에는 paymentKey가 필요합니다. paymentId: {}, orderId: {}", 
                    payment.getId(), payment.getOrderId());
            throw new BaseException(PaymentErrorCode.PAYMENT_KEY_REQUIRED);
        }

        Map<String, Object> cancelRequest = new HashMap<>();
        cancelRequest.put("paymentKey", paymentKey);
        cancelRequest.put("cancelAmount", refundAmount);
        cancelRequest.put("cancelReason", request.reason() != null && !request.reason().isBlank()
                ? request.reason()
                : "USER_REFUND");

        try {
            String refundResponse = restClient.post()
                    .uri(targetUrl + "/cancel")
                    .headers(headers -> headers.set("Authorization", createAuthorizationHeader()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cancelRequest)
                    .retrieve()
                    .body(String.class);
            log.debug("토스 결제 환불 성공 - paymentKey: {}, refundAmount: {}", paymentKey, refundAmount);
            return refundResponse;
        } catch (Exception e) {
            log.error("토스 결제 환불 요청에 실패했습니다. paymentKey: {}, refundAmount: {}", 
                    paymentKey, refundAmount, e);
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_REFUND_FAILED);
        }
    }

    private void notifyOrderRefund(String orderCode) {
        try {
            orderServiceClient.updateOrderStatus(orderCode, "REFUNDED");
        } catch (Exception e) {
            log.error("주문 서비스에 환불 상태를 전달하는 데 실패했습니다. orderCode: {}", orderCode, e);
            throw new BaseException(PaymentErrorCode.ORDER_SERVICE_NOTIFICATION_FAILED);
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
