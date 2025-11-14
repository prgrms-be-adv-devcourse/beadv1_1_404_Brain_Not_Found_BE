package com.example.core.model.vo.kafka;

import com.example.core.model.vo.kafka.enums.SettlementCompleteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SettlementCompleteEvent (
        @NotBlank(message = "SelleCode 는 공백이거나 null일 수 없습니다.")
        String selleCode,
        @NotBlank(message = "OrderItemCode 는 공백이거나 null일 수 없습니다.")
        String orderItemCode,
        @NotNull(message = "amount 는 필수입력값입니다.")
        @PositiveOrZero(message = "금액는 0 이상이어야 커야 합니다.")
        Long amount,
        @NotNull(message = "type 은 필수입력값입니다.")
        SettlementCompleteType type
) {
    public static SettlementCompleteEvent settlementFrom(String selleCode, String orderItemCode, Long amount) {
        return new SettlementCompleteEvent(selleCode, orderItemCode, amount, SettlementCompleteType.SETTLEMENT);
    }

    public static SettlementCompleteEvent refundFrom(String selleCode, String orderItemCode, Long amount) {
        return new SettlementCompleteEvent(selleCode, orderItemCode, amount, SettlementCompleteType.REFUND);
    }

}
