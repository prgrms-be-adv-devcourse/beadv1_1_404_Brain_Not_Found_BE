package com.ll.products.domain.recommendation.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class VectorIndexException extends BaseException {
    public VectorIndexException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}