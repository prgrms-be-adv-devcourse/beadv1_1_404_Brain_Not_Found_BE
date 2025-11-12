package com.example.deposit.model.vo.response;

import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.enums.DepositStatus;

import java.time.LocalDateTime;

public record DepositDeleteResponse(
        String userCode,
        String depositCode,
        Long balance,
        DepositStatus depositStatus,
        String closedReason,
        LocalDateTime updatedAt
) {
    public static DepositDeleteResponse from(String userCode, Deposit deposit, String closedReason) {
        return new DepositDeleteResponse(
                userCode,
                deposit.getCode(),
                deposit.getBalance(),
                deposit.getDepositStatus(),
                closedReason,
                deposit.getUpdatedAt()
        );
    }
}
