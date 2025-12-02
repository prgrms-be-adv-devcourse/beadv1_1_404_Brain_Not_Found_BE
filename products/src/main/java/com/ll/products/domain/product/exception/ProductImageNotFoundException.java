package com.ll.products.domain.product.exception;

import com.ll.core.model.exception.BaseErrorCode;
import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class ProductImageNotFoundException extends BaseException {
    public ProductImageNotFoundException(String fileKey) {
        super(ErrorCode.NOT_FOUND,
                String.format("존재하지 않는 이미지 입니다. fileKey: %s", fileKey));
    }
}
