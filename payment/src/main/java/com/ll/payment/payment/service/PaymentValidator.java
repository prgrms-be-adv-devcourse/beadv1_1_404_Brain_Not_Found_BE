package com.ll.payment.payment.service;

import com.ll.core.model.exception.BaseException;
import com.ll.payment.payment.exception.PaymentErrorCode;
import com.ll.payment.payment.model.entity.Payment;
import com.ll.payment.payment.model.enums.PaymentStatus;
import com.ll.payment.payment.model.vo.request.PaymentRefundRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentValidator {

    public int validateRefundEligibility(Payment payment, PaymentRefundRequest request) {
        if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
            log.warn("환불은 완료된 결제만 가능합니다. paymentId: {}, status: {}",
                    payment.getId(), payment.getPaymentStatus());
            throw new BaseException(PaymentErrorCode.REFUND_NOT_ELIGIBLE);
        }

        if (request.orderId() != null && !request.orderId().equals(payment.getOrderId())) {
            log.warn("주문 번호가 결제 정보와 일치하지 않습니다. 요청 orderId: {}, 결제 orderId: {}",
                    request.orderId(), payment.getOrderId());
            throw new BaseException(PaymentErrorCode.ORDER_ID_MISMATCH);
        }
        if (request.orderCode() == null || request.orderCode().isBlank()) {
            log.warn("환불에는 orderCode가 필요합니다.");
            throw new BaseException(PaymentErrorCode.ORDER_CODE_REQUIRED);
        }

        if (request.paidType() != null && request.paidType() != payment.getPaidType()) {
            log.warn("요청한 결제 수단이 실제 결제 수단과 다릅니다. 요청: {}, 실제: {}",
                    request.paidType(), payment.getPaidType());
            throw new BaseException(PaymentErrorCode.PAYMENT_TYPE_MISMATCH);
        }

        int refundAmount = request.refundAmount() != null ? request.refundAmount() : payment.getPaidAmount();
        if (refundAmount <= 0) {
            log.warn("환불 금액이 0 이하입니다. refundAmount: {}", refundAmount);
            throw new BaseException(PaymentErrorCode.REFUND_AMOUNT_INVALID);
        }
        if (refundAmount > payment.getPaidAmount()) {
            log.warn("환불 금액이 결제 금액을 초과합니다. refundAmount: {}, paidAmount: {}",
                    refundAmount, payment.getPaidAmount());
            throw new BaseException(PaymentErrorCode.REFUND_AMOUNT_EXCEEDED);
        }
        if (refundAmount != payment.getPaidAmount()) {
            log.warn("부분 환불은 현재 지원되지 않습니다. refundAmount: {}, paidAmount: {}",
                    refundAmount, payment.getPaidAmount());
            throw new BaseException(PaymentErrorCode.PARTIAL_REFUND_NOT_SUPPORTED);
        }

        return refundAmount;
    }
}

