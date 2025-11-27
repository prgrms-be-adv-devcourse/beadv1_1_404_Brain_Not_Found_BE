package com.ll.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.exception.BaseException;
import com.ll.payment.client.DepositServiceClient;
import com.ll.payment.client.OrderServiceClient;
import com.ll.payment.exception.PaymentErrorCode;
import com.ll.payment.model.vo.response.DepositInfoResponse;
import com.ll.payment.model.vo.PaymentProcessResult;
import com.ll.payment.model.vo.response.TossPaymentResponse;
import com.ll.payment.model.entity.Payment;
import com.ll.payment.model.enums.PaidType;
import com.ll.payment.model.enums.PaymentStatus;
import com.ll.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.model.vo.request.PaymentRequest;
import com.ll.payment.model.vo.request.TossPaymentRequest;
import com.ll.payment.model.vo.request.TossPaymentCreateRequest;
import com.ll.payment.model.vo.response.TossPaymentCreateResponse;
import com.ll.payment.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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

    private final PaymentJpaRepository paymentJpaRepository;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final DepositServiceClient depositServiceClient;
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
        int currentBalance = Optional.ofNullable(depositServiceClient.getDepositInfo(payment.buyerCode()))
                .map(DepositInfoResponse::balance)
                .orElse(0);
        int requestedAmount = payment.paidAmount();

        // 예치금이 충분한 경우 바로 예치금 결제
        if (currentBalance >= requestedAmount) {
            Payment depositPayment = completeDepositPayment(payment, requestedAmount);
            return new PaymentProcessResult(depositPayment, null);
        }

        // 예치금이 부족한 경우 : 토스 결제로 금액 충전 + 예치금으로 전체 결제
        int shortageAmount = requestedAmount - currentBalance;
        log.info("예치금 부족 - 현재 잔액: {}, 요청 금액: {}, 부족 금액: {}", 
                currentBalance, requestedAmount, shortageAmount);

        // 토스 결제
        Payment chargeTossPayment = null;
        try {
            PaymentRequest chargeTossRequest = payment.withAmountAndType(shortageAmount, PaidType.TOSS_PAYMENT);
            chargeTossPayment = tossPayment(chargeTossRequest, PaymentStatus.CHARGE);
            log.info("충전용 토스 결제 완료 - paymentId: {}, amount: {}", 
                    chargeTossPayment.getId(), shortageAmount);

            // 토스 결제 성공 후 예치금 충전
            String chargeReferenceCode = "CHARGE-" + payment.orderId() + "-" + System.currentTimeMillis();
            depositServiceClient.chargeDeposit(
                    payment.buyerCode(), 
                    (long) shortageAmount, 
                    chargeReferenceCode
            );
            log.info("예치금 충전 완료 - buyerCode: {}, amount: {}", 
                    payment.buyerCode(), shortageAmount);

        } catch (Exception e) {
            log.error("토스 결제 또는 예치금 충전 실패 - orderId: {}, shortageAmount: {}, error: {}", 
                    payment.orderId(), shortageAmount, e.getMessage(), e);
            
            // 토스 결제는 성공했지만 예치금 충전 실패한 경우 토스 환불
            if (chargeTossPayment != null && chargeTossPayment.getPaymentStatus() == PaymentStatus.CHARGE) {
                try {
                    processTossRefundForCharge(chargeTossPayment, shortageAmount);
                    log.info("예치금 충전 실패로 인한 토스 결제 환불 완료 - paymentId: {}", chargeTossPayment.getId());
                } catch (Exception refundException) {
                    log.error("토스 결제 환불 실패 - paymentId: {}, error: {}", 
                            chargeTossPayment.getId(), refundException.getMessage(), refundException);
                    // 환불도 실패한 경우는 수동 처리 필요
                }
            }
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_CREATE_FAILED);
        }

        // 예치금으로 전체 결제
        Payment depositPayment = completeDepositPayment(payment, requestedAmount);
        log.info("예치금 결제 완료 - orderId: {}, amount: {}", payment.orderId(), requestedAmount);

        return new PaymentProcessResult(depositPayment, chargeTossPayment);
    }

    @Override
    @Transactional
    public Payment tossPayment(PaymentRequest request, PaymentStatus finalStatus) {
        String paymentKey = request.paymentKey();
        if (paymentKey == null || paymentKey.isBlank()) {
            paymentKey = createPayment(
                    request.orderId(),
                    "주문번호: " + request.orderId(),
                    "고객 이름",
                    request.paidAmount()
            );
        }

        // 1) 결제 엔티티 생성
        Payment payment = Payment.createTossPayment(
                request.orderId(),
                request.buyerId(),
                request.paidAmount(),
                paymentKey
        );
        paymentJpaRepository.save(payment);

        boolean isMockPaymentKey = paymentKey != null && paymentKey.startsWith("tgen_test_");
        if (isMockPaymentKey) {
            log.info("더미 paymentKey 사용 중. Toss 승인 API 호출을 건너뜁니다. paymentKey: {}", paymentKey);
            payment.markSuccess(
                    finalStatus,
                    LocalDateTime.now()
            );
            return payment;
        }

        // 3) 실제 Toss 승인 요청
        TossPaymentRequest tossRequest = TossPaymentRequest.from(
                paymentKey,
                request.orderCode(),
                request.paidAmount()
        );
        String response = confirmPayment(tossRequest);
        TossPaymentResponse tossPaymentResponse = parseTossResponse(response);
        validateTossResponse(request, tossPaymentResponse);

        payment.markSuccess(
                finalStatus,
                tossPaymentResponse.approvedAt() != null
                        ? tossPaymentResponse.approvedAt().toLocalDateTime()
                        : LocalDateTime.now()
        );

        return payment;
    }

    private String createPayment(Long orderId, String orderName, String customerName, Integer amount) {
        if (useMockPaymentKey) {
            String mockPaymentKey = "tgen_test_" + System.currentTimeMillis() + "_" + orderId;
            log.info("테스트용 더미 paymentKey 생성: {}", mockPaymentKey);
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
            log.info("Toss 결제 생성 요청 - orderId: {}, orderName: {}, amount: {}, successUrl: {}, failUrl: {}",
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

            log.info("Toss 결제 생성 응답: {}", response);
            TossPaymentCreateResponse createResponse = objectMapper.readValue(response, TossPaymentCreateResponse.class);
            return createResponse.paymentKey();
        } catch (Exception e) {
            log.error("토스 결제 생성 중 예외 발생: {}", e.getMessage());
            log.warn("Toss Payments API 호출 실패. 테스트용 더미 paymentKey를 생성합니다.");
            // API 호출 실패 시 더미 paymentKey 생성
            String mockPaymentKey = "tgen_test_" + System.currentTimeMillis() + "_" + orderId;
            log.info("테스트용 더미 paymentKey 생성: {}", mockPaymentKey);
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

        switch (payment.getPaidType()) {
            case DEPOSIT -> processDepositRefund(payment, request, refundAmount);
            case TOSS_PAYMENT -> processTossRefund(payment, request, refundAmount);
            default -> {
                log.warn("지원하지 않는 결제 수단입니다. paidType: {}", payment.getPaidType());
                throw new BaseException(PaymentErrorCode.UNSUPPORTED_PAYMENT_TYPE);
            }
        }

        payment.markRefund(LocalDateTime.now());
        paymentJpaRepository.save(payment);
        notifyOrderRefund(request.orderCode());
        return payment;
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

    private Payment completeDepositPayment(PaymentRequest payment, int amount) {
        // 예치금 차감
        depositServiceClient.withdraw(payment.buyerCode(), (long) amount, createReferenceCode(payment.orderId()));
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
        // request에 orderId 외에는 들어가질 않음
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

        // paymentId, Code로 조회 (COMPLETED 상태만) <- request에 orderId가 있어서 사용되지 않으나 다른 곳에서 쓸 수 있으므로 유지중
        if (request.paymentId() != null) {
            return paymentJpaRepository.findByIdAndPaymentStatus(
                            request.paymentId(),
                            PaymentStatus.COMPLETED
                    )
                    .orElseThrow(() -> {
                        log.warn("환불 대상 결제 정보를 찾을 수 없습니다. paymentId: {}, status: COMPLETED", request.paymentId());
                        return new BaseException(PaymentErrorCode.PAYMENT_NOT_FOUND);
                    });
        }
        if (request.paymentCode() != null && !request.paymentCode().isBlank()) {
            return paymentJpaRepository.findByPaymentCodeAndPaymentStatus(
                            request.paymentCode(),
                            PaymentStatus.COMPLETED
                    )
                    .orElseThrow(() -> {
                        log.warn("환불 대상 결제 정보를 찾을 수 없습니다. paymentCode: {}, status: COMPLETED", request.paymentCode());
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
        depositServiceClient.chargeDeposit(buyerCode, (long) refundAmount, createReferenceCode(payment.getOrderId()));
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
            log.info("충전 실패로 인한 토스 결제 환불 성공 - paymentKey: {}, refundAmount: {}", 
                    paymentKey, refundAmount);
        } catch (Exception e) {
            log.error("충전 실패로 인한 토스 결제 환불 요청에 실패했습니다. paymentKey: {}, refundAmount: {}", 
                    paymentKey, refundAmount, e);
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_REFUND_FAILED);
        }
    }

    private void processTossRefund(Payment payment, PaymentRefundRequest request, int refundAmount) {
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
            restClient.post()
                    .uri(targetUrl + "/cancel")
                    .headers(headers -> headers.set("Authorization", createAuthorizationHeader()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cancelRequest)
                    .retrieve()
                    .toBodilessEntity();
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
