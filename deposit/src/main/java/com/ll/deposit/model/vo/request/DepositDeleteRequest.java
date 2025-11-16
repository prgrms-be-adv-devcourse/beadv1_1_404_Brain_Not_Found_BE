package com.ll.deposit.model.vo.request;

import jakarta.validation.constraints.NotBlank;

public record DepositDeleteRequest (
        @NotBlank(message = "closedReason 는 공백이거나 null일 수 없습니다.")
        String closedReason
){
}
