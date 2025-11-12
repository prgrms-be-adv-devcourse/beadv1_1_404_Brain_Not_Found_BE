package com.ll.products.domain.product.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String code) {
        super("상품 코드 " + code + "에 해당하는 상품을 찾을 수 없습니다.");
    }
}
