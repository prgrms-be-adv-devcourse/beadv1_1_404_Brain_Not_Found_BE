package com.ll.core.model.exception;

public class InvalidDateRangeException extends BaseException {

    public InvalidDateRangeException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public InvalidDateRangeException(String customMessage) {
        super(ErrorCode.BAD_REQUEST, customMessage);
    }
}
