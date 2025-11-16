package com.example.core.model.exception;

public class InvalidSettlementEventException extends BaseException {

    public InvalidSettlementEventException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public InvalidSettlementEventException(String customMessage) {
        super(ErrorCode.BAD_REQUEST, customMessage);
    }
}
