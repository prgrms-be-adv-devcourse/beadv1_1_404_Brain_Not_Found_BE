package com.example.deposit.model.exception;

import com.example.core.exception.BaseException;
import com.example.core.exception.ErrorCode;

public class InvalidDateRangeException extends BaseException {

    public InvalidDateRangeException() {
        super(ErrorCode.BAD_REQUEST);
    }

    public InvalidDateRangeException(String customMessage) {
        super(ErrorCode.BAD_REQUEST, customMessage);
    }
}
