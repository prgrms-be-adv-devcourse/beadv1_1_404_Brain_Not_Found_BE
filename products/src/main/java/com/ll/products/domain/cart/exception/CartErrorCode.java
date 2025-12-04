package com.ll.products.domain.cart.exception;

import com.ll.core.model.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CartErrorCode implements BaseErrorCode {

    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니를 찾을 수 없거나 활성 상태가 아닙니다."),

    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 아이템을 찾을 수 없거나 활성 상태의 장바구니가 아닙니다."),

    CART_ITEM_NOT_BELONG_TO_CART(HttpStatus.BAD_REQUEST, "해당 장바구니에 속하지 않은 아이템입니다."),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),

    INSUFFICIENT_PRODUCT_STOCK(HttpStatus.BAD_REQUEST, "상품 재고가 부족합니다."),

    PRODUCT_PRICE_MISMATCH(HttpStatus.BAD_REQUEST, "상품 가격이 일치하지 않습니다.");

    private final HttpStatus status;
    private final String message;

    CartErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
