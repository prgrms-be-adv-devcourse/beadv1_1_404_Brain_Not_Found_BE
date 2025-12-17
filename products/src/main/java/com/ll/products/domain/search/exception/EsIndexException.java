package com.ll.products.domain.search.exception;

import com.ll.core.model.exception.BaseErrorCode;
import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class EsIndexException extends BaseException {
    public EsIndexException(String customMessage) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, customMessage);
    }
}
