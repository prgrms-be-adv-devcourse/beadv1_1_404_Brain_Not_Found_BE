package com.ll.payment.deposit.model.vo.response;

import com.ll.payment.deposit.model.entity.DepositHistory;
import com.ll.payment.deposit.model.enums.DepositHistoryType;

import java.time.LocalDateTime;

public record DepositTransactionResponse(
        String userCode,
        String depositCode,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        DepositHistoryType historyType,
        String referenceCode,
        LocalDateTime createdAt
) {
    public static DepositTransactionResponse from(String depositCode, DepositHistory depositHistory) {
        return new DepositTransactionResponse(
                depositHistory.getCode(),
                depositCode,
                depositHistory.getAmount(),
                depositHistory.getBalanceBefore(),
                depositHistory.getBalanceAfter(),
                depositHistory.getHistoryType(),
                depositHistory.getReferenceCode(),
                depositHistory.getCreatedAt()
        );
    }
}
