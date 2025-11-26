package com.ll.payment.exception;

import com.ll.core.model.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PaymentErrorCode implements BaseErrorCode {

    // 404 — 리소스 없음
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),

    // 400 — 잘못된 요청
    UNSUPPORTED_PAYMENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 결제 수단입니다."),
    ORDER_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "환불에는 orderCode가 필요합니다."),
    BUYER_CODE_REQUIRED(HttpStatus.BAD_REQUEST, "예치금 환불에는 buyerCode가 필요합니다."),
    PAYMENT_KEY_REQUIRED(HttpStatus.BAD_REQUEST, "토스 환불에는 paymentKey가 필요합니다."),
    ORDER_ID_MISMATCH(HttpStatus.BAD_REQUEST, "주문 번호가 결제 정보와 일치하지 않습니다."),
    PAYMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청한 결제 수단이 실제 결제 수단과 다릅니다."),
    REFUND_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "환불 금액이 0 이하입니다."),
    REFUND_AMOUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "환불 금액이 결제 금액을 초과합니다."),
    PARTIAL_REFUND_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "부분 환불은 현재 지원되지 않습니다."),
    REFUND_TARGET_NOT_FOUND(HttpStatus.BAD_REQUEST, "환불 대상 결제 정보를 찾을 수 없습니다."),

    // 409 — 충돌 발생
    REFUND_NOT_ELIGIBLE(HttpStatus.CONFLICT, "환불은 완료된 결제만 가능합니다."),
    TOSS_PAYMENT_STATUS_INVALID(HttpStatus.CONFLICT, "토스 결제 승인 상태가 DONE이 아닙니다."),
    TOSS_PAYMENT_AMOUNT_MISMATCH(HttpStatus.CONFLICT, "토스 승인 금액과 요청 금액이 일치하지 않습니다."),

    // 500 — 내부 서버 오류
    TOSS_PAYMENT_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Toss 결제 생성 API 호출에 실패했습니다."),
    TOSS_PAYMENT_RESPONSE_PARSE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토스 결제 응답 파싱에 실패했습니다."),
    TOSS_PAYMENT_REFUND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토스 결제 환불 요청에 실패했습니다."),
    ORDER_SERVICE_NOTIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주문 서비스에 환불 상태를 전달하는 데 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    PaymentErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

