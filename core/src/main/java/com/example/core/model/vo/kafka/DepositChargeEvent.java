package com.example.core.model.vo.kafka;

public record DepositChargeEvent (
        Long userId,
        String userCode,
        Long amount,
        String referenceCode
){
}
