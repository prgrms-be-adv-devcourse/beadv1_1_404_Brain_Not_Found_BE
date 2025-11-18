package com.ll.products.domain.product.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class ProductOwnershipException extends BaseException {
    public ProductOwnershipException(Long userId, String productCode) {
        super(ErrorCode.FORBIDDEN,
              String.format("상품 권한 없음 : {}", productCode));
    }
}