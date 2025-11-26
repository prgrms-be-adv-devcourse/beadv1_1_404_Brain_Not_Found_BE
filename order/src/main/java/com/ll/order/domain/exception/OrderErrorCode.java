package com.ll.order.domain.exception;

import com.ll.core.model.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderErrorCode implements BaseErrorCode {

    // 404 — 리소스 없음
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 400 — 잘못된 요청
    UNSUPPORTED_PAYMENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 결제 수단입니다."),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "장바구니가 비어있습니다."),
    DUPLICATE_PRODUCT_CODE(HttpStatus.BAD_REQUEST, "중복된 상품 코드가 포함되어 있습니다."),

    // 409 — 충돌 발생
    ORDER_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 주문입니다."),
    INVALID_ORDER_STATUS_TRANSITION(HttpStatus.CONFLICT, "해당 상태로 전환할 수 없습니다."),

    // 422 — 처리 불가능한 엔티티
    INSUFFICIENT_INVENTORY(HttpStatus.UNPROCESSABLE_ENTITY, "재고가 부족합니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.UNPROCESSABLE_ENTITY, "판매 중이 아닌 상품입니다."),
    PRODUCT_PRICE_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "요청한 상품 가격이 실제 가격과 일치하지 않습니다."),

    // 500 — 내부 서버 오류
    PAYMENT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 처리에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    OrderErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}

