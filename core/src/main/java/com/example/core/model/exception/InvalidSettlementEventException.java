package com.example.core.model.exception;

import com.example.core.exception.BaseException;
import com.example.core.exception.ErrorCode;

public class InvalidSettlementEventException extends BaseException {

    public InvalidSettlementEventException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public InvalidSettlementEventException(String customMessage) {
        super(ErrorCode.BAD_REQUEST, customMessage);
    }
}
