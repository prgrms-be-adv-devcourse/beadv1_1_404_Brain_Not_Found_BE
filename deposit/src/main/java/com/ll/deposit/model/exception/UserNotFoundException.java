package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException() {
        super(ErrorCode.NOT_FOUND);
    }

    public UserNotFoundException(String customMessage) {
        super(ErrorCode.NOT_FOUND, customMessage);
    }
}
