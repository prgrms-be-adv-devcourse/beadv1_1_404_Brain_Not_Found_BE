package com.example.settlement.model.exception;

import com.example.core.model.exception.BaseException;
import com.example.core.model.exception.ErrorCode;

public class SettlementStateTransitionException extends BaseException {

    public SettlementStateTransitionException() {
        super(ErrorCode.SETTLEMENT_INVALID_STATE_TRANSITION);
    }

    public SettlementStateTransitionException(String customMessage) {
        super(ErrorCode.SETTLEMENT_INVALID_STATE_TRANSITION, customMessage);
    }
}
