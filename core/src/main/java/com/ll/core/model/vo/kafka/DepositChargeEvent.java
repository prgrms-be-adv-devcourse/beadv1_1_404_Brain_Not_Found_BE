package com.ll.core.model.vo.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepositChargeEvent (
        @NotBlank(message = "userCode 는 공백이거나 null일 수 없습니다.")
        String userCode,
        @NotNull(message = "amount 는 필수입력값입니다.")
        Long amount,
        @NotBlank(message = "referenceCode 는 공백이거나 null일 수 없습니다.")
        String referenceCode
){
}
