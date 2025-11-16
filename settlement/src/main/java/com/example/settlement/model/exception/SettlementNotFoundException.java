package com.example.settlement.model.exception;

import com.example.core.model.exception.BaseException;
import com.example.core.model.exception.ErrorCode;

public class SettlementNotFoundException extends BaseException {

    public SettlementNotFoundException() {
        super(ErrorCode.SETTLEMENT_NOT_FOUND);
    }

    public SettlementNotFoundException(String customMessage) {
        super(ErrorCode.SETTLEMENT_NOT_FOUND, customMessage);
    }
}
