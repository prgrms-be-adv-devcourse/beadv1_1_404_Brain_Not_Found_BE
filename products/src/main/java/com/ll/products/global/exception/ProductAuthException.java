package com.ll.products.global.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class ProductAuthException extends BaseException {
    public ProductAuthException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}