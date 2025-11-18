package com.ll.products.domain.product.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class InsufficientInventoryException extends BaseException {

    public InsufficientInventoryException(String productCode, Integer availableQuantity) {
        super(ErrorCode.INSUFFICIENT_INVENTORY,
                String.format("재고가 부족합니다. 상품코드: %s, 현재재고: %d", productCode, availableQuantity));
    }
}
