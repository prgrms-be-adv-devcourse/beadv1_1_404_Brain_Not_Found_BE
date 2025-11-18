package com.ll.products.domain.product.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class ProductNotFoundException extends BaseException {

    public ProductNotFoundException(String code) {
        super(ErrorCode.NOT_FOUND,
                String.format("상품을 찾을 수 없습니다. 상품코드: %s", code));
    }
}
