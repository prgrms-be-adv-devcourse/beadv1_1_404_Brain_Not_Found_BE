package com.ll.core.model.vo.kafka;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SettlementEvent(
        @NotNull(message = "settlementId 는 필수입력값입니다.")
        Long settlementId,
        @NotBlank(message = "SelleCode 는 공백이거나 null일 수 없습니다.")
        String sellerCode,
        @NotBlank(message = "OrderItemCode 는 공백이거나 null일 수 없습니다.")
        String orderItemCode,
        @NotNull(message = "amount 는 필수입력값입니다.")
        @PositiveOrZero(message = "금액는 0 이상이어야 커야 합니다.")
        Long amount
) {
    public static SettlementEvent from(Long settlementId, String sellerCode, String orderItemCode, Long amount) {
        return new SettlementEvent(settlementId, sellerCode, orderItemCode, amount);
    }
}
