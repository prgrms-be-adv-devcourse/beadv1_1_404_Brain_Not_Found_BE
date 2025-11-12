package com.example.deposit.model.vo.response;

import com.example.deposit.model.entity.DepositHistory;
import com.example.deposit.model.enums.DepositHistoryType;
import com.example.deposit.model.enums.TransactionStatus;

import java.time.LocalDateTime;

public record DepositHistoryResponse (
        String depositHistoryCode,
        String depositCode,
        Long amount,
        Long balanceBefore,
        Long balanceAfter,
        DepositHistoryType historyType,
        TransactionStatus transactionStatus,
        String referenceCode,
        LocalDateTime updatedAt
) {
    public static DepositHistoryResponse from(DepositHistory depositHistory) {
        return new DepositHistoryResponse(
                depositHistory.getCode(),
                depositHistory.getDeposit().getCode(),
                depositHistory.getAmount(),
                depositHistory.getBalanceBefore(),
                depositHistory.getBalanceAfter(),
                depositHistory.getHistoryType(),
                depositHistory.getTransactionStatus(),
                depositHistory.getReferenceCode(),
                depositHistory.getUpdatedAt()
        );
    }
}