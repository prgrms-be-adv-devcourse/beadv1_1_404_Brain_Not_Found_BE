package com.ll.payment.deposit.model.exception;

import com.ll.core.model.exception.BaseException;

public class InvalidDepositStatusTransitionException extends BaseException {

    public InvalidDepositStatusTransitionException() {
        super(ErrorCode.CAN_NOT_TRANSACT_ON_INACTIVE_DEPOSIT);
    }

    public InvalidDepositStatusTransitionException(String customMessage) {
        super(ErrorCode.CAN_NOT_TRANSACT_ON_INACTIVE_DEPOSIT, customMessage);
    }
}
