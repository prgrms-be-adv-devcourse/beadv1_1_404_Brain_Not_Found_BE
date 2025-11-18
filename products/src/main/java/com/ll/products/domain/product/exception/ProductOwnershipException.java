package com.ll.products.domain.product.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class ProductOwnershipException extends BaseException {
    public ProductOwnershipException(String userCode, String productCode) {
        super(ErrorCode.FORBIDDEN,
                String.format("상품에 대한 권한이 없습니다. 상품코드: %s", productCode));
    }
}