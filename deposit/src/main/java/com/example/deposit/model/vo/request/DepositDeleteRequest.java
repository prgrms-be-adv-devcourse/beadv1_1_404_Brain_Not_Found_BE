package com.example.deposit.model.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepositDeleteRequest (
        @NotNull(message = "closedReason 는 필수입력값입니다.")
        @NotBlank(message = "closedReason 는 공백일 수 없습니다.")
        String closedReason
){
}
