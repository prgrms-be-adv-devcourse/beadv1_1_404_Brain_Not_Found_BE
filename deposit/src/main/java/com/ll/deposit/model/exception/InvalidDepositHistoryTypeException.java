package com.ll.deposit.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class InvalidDepositHistoryTypeException extends BaseException {

    public InvalidDepositHistoryTypeException() {
        super(ErrorCode.INVALID_DEPOSIT_HISTORY_TYPE);
    }

    public InvalidDepositHistoryTypeException(String customMessage) {
        super(ErrorCode.INVALID_DEPOSIT_HISTORY_TYPE, customMessage);
    }
}
