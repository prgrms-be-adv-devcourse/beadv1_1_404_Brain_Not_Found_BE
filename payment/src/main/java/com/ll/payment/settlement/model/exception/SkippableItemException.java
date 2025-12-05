package com.ll.payment.settlement.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class SkippableItemException extends BaseException {

    public SkippableItemException() {
        super(ErrorCode.UNPROCESSABLE_ENTITY);
    }

    public SkippableItemException(String customMessage) {
        super(ErrorCode.UNPROCESSABLE_ENTITY, customMessage);
    }
}
