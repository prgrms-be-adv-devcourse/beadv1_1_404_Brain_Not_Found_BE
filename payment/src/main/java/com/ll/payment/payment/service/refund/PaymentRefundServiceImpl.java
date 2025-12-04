package com.ll.payment.payment.service.refund;

import com.ll.core.model.exception.BaseException;
import com.ll.payment.deposit.model.vo.request.DepositTransactionRequest;
import com.ll.payment.deposit.service.DepositService;
import com.ll.payment.global.client.OrderServiceClient;
import com.ll.payment.payment.exception.PaymentErrorCode;
import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.entity.PaymentHistoryEntity;
import com.ll.payment.payment.model.enums.PaidType;
import com.ll.payment.payment.model.enums.PaymentHistoryActionType;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;
import com.ll.payment.payment.repository.PaymentHistoryJpaRepository;
import com.ll.payment.payment.repository.PaymentJpaRepository;
import com.ll.payment.payment.service.PaymentValidator;
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
    private final OrderServiceClient orderServiceClient;
    private final RestClient restClient;

    @Value("${payment.secretKey}")
    private String secretKey;
    @Value("${payment.targetUrl}")
    private String targetUrl;

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

    @Override
    public void processTossRefundForCharge(Payment payment, int refundAmount) {
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
        depositService.chargeDeposit(
                buyerCode,
                new DepositTransactionRequest((long) refundAmount, createReferenceCode(payment.getOrderId()))
        );
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

