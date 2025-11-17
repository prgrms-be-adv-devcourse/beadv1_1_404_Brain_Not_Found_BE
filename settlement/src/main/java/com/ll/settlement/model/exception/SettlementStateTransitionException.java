package com.ll.settlement.model.exception;

import com.ll.core.model.exception.BaseException;
import com.ll.core.model.exception.ErrorCode;

public class SettlementStateTransitionException extends BaseException {

    public SettlementStateTransitionException() {
        super(ErrorCode.SETTLEMENT_INVALID_STATE_TRANSITION);
    }

    public SettlementStateTransitionException(String customMessage) {
        super(ErrorCode.SETTLEMENT_INVALID_STATE_TRANSITION, customMessage);
    }
}
