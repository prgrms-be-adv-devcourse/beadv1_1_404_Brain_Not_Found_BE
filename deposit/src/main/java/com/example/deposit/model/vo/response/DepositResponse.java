package com.example.deposit.model.vo.response;

import com.example.deposit.model.entity.Deposit;
import com.example.deposit.model.enums.DepositStatus;

import java.time.LocalDateTime;

public record DepositResponse (
        String userCode,
        String depositCode,
        Long balance,
        DepositStatus depositStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DepositResponse from(String userCode, Deposit deposit) {
        return new DepositResponse(
                userCode,
                deposit.getCode(),
                deposit.getBalance(),
                deposit.getDepositStatus(),
                deposit.getCreatedAt(),
                deposit.getUpdatedAt()
        );
    }
}
