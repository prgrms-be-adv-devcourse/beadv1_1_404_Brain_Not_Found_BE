package com.example.deposit.model.vo.response;

import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.model.enums.DepositHistoryType;
import com.example.deposit.model.enums.TransactionStatus;

import java.time.LocalDateTime;

public record DepositTransactionResponse(
        String userCode,
        String depositCode,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        DepositHistoryType historyType,
        TransactionStatus transactionStatus,
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
                depositHistory.getTransactionStatus(),
                depositHistory.getReferenceCode(),
                depositHistory.getCreatedAt()
        );
    }
}
