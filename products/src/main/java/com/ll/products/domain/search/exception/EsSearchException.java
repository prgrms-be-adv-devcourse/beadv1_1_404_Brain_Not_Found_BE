package com.ll.products.domain.search.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class EsSearchException extends BaseException {
    public EsSearchException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
