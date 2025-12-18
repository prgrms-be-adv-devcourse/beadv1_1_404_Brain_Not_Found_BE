package com.ll.payment.payment.service.refund;

import com.ll.core.model.exception.BaseException;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.service.DepositService;
import com.ll.payment.payment.exception.PaymentErrorCode;
import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.entity.PaymentHistoryEntity;
import com.ll.payment.payment.model.enums.PaymentRefundNotificationStatus;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.payment.repository.PaymentHistoryJpaRepository;
import com.ll.payment.payment.repository.PaymentJpaRepository;
import com.ll.payment.payment.service.PaymentValidator;
import com.ll.payment.payment.service.event.PaymentRefundNotificationOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRefundServiceImpl implements PaymentRefundService {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentHistoryJpaRepository paymentHistoryJpaRepository;
    private final DepositService depositService;
    private final PaymentValidator paymentValidator;
    private final PaymentRefundNotificationOutboxService paymentRefundNotificationOutboxService;
    private final RestClient restClient;

    @Value("${payment.secretKey}")
    private String secretKey;
    @Value("${payment.targetUrl}")
    private String targetUrl;

    @Override
    @Transactional
    public Payment refundPayment(PaymentRefundRequest request) {
        Payment payment = findPaymentForRefund(request);
        int refundAmount = paymentValidator.validateRefund(payment, request);

        // 환불 요청 이력 저장
        PaymentHistoryEntity refundRequestHistory = PaymentHistoryEntity.createRefundRequestHistory(payment, refundAmount);
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

            payment.refund(LocalDateTime.now());
            paymentJpaRepository.save(payment);

            // 환불 완료 이력 저장
            PaymentHistoryEntity refundDoneHistory = PaymentHistoryEntity.createRefundDoneHistory(payment, refundAmount, refundResponse);
            paymentHistoryJpaRepository.save(refundDoneHistory);

            // Outbox 패턴: 트랜잭션 내에서 먼저 Outbox에 저장 (PENDING 상태)
            // 별도 프로세스가 Outbox를 읽어서 주문 서비스에 알림 전송
            paymentRefundNotificationOutboxService.saveToOutbox(
                    request.orderCode(), 
                    PaymentRefundNotificationStatus.REFUNDED.getValue()
            );
            
            return payment;
        } catch (Exception e) {
            // 환불 실패 이력 저장
            PaymentHistoryEntity refundFailHistory = PaymentHistoryEntity.createRefundFailHistory(payment, refundAmount, e.getMessage());
            paymentHistoryJpaRepository.save(refundFailHistory);

            // 환불 실패하면 order 모듈에게 알림 전송하여 보상 로직 트리거 작동
            try {
                paymentRefundNotificationOutboxService.saveToOutbox(
                        request.orderCode(), 
                        PaymentRefundNotificationStatus.REFUND_FAILED.getValue()
                );
                log.debug("환불 실패 알림 Outbox 저장 완료 - orderCode: {}, error: {}", request.orderCode(), e.getMessage());
            } catch (Exception notificationException) {
                log.error("환불 실패 알림 Outbox 저장 실패 - orderCode: {}, error: {}", 
                        request.orderCode(), notificationException.getMessage(), notificationException);
            }

            throw e;
        }
    }

    @Override
    public void processTossRefundForCharge(Payment payment, int refundAmount) {
        tossRefund(payment, refundAmount, "예치금 충전 실패로 인한 환불", false);
        log.debug("충전 실패로 인한 토스 결제 환불 성공 - paymentId: {}, refundAmount: {}", 
                payment.getId(), refundAmount);
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
        
        if (request.paymentCode() != null && !request.paymentCode().isBlank()) {
            return paymentJpaRepository.findByCode(request.paymentCode())
                    .orElseThrow(() -> {
                        log.warn("환불 대상 결제 정보를 찾을 수 없습니다. paymentCode: {}", request.paymentCode());
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
        depositService.refundDeposit(
                buyerCode,
                new DepositTransactionRequest((long) refundAmount, createReferenceCode(payment.getOrderId()))
        );
    }

    private String processTossRefund(Payment payment, PaymentRefundRequest request, int refundAmount) {
        String cancelReason = request.reason() != null && !request.reason().isBlank()
                ? request.reason()
                : "USER_REFUND";
        String refundResponse = tossRefund(payment, refundAmount, cancelReason, true);
        log.debug("토스 결제 환불 성공 - paymentId: {}, refundAmount: {}", payment.getId(), refundAmount);
        return refundResponse;
    }

    private String tossRefund(Payment payment, int refundAmount, String cancelReason, boolean returnResponse) {
        String paymentKey = payment.getPaymentKey();
        if (paymentKey == null || paymentKey.isBlank()) {
            log.warn("토스 환불에는 paymentKey가 필요합니다. paymentId: {}, orderId: {}", 
                    payment.getId(), payment.getOrderId());
            throw new BaseException(PaymentErrorCode.PAYMENT_KEY_REQUIRED);
        }
        String cancelUrl = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        Map<String, Object> cancelRequest = new HashMap<>();
        cancelRequest.put("cancelAmount", refundAmount);
        cancelRequest.put("cancelReason", cancelReason);

        try {
            var requestSpec = restClient.post()
                    .uri(cancelUrl)
                    .headers(headers -> headers.set("Authorization", createAuthorizationHeader()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cancelRequest)
                    .retrieve();

            if (returnResponse) {
                return requestSpec.body(String.class);
            } else {
                requestSpec.toBodilessEntity();
                return null;
            }
        } catch (Exception e) {
            log.error("토스 결제 환불 요청에 실패했습니다. paymentKey: {}, refundAmount: {}, cancelReason: {}", 
                    paymentKey, refundAmount, cancelReason, e);
            throw new BaseException(PaymentErrorCode.TOSS_PAYMENT_REFUND_FAILED);
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

