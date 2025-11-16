package com.example.deposit.model.exception;

import com.example.core.model.exception.BaseException;
import com.example.core.model.exception.ErrorCode;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }

    public UserNotFoundException(String customMessage) {
        super(ErrorCode.NOT_FOUND, customMessage);
    }
}
