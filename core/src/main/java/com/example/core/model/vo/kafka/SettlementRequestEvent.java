package com.example.core.model.vo.kafka;

import com.example.core.model.exception.InvalidSettlementEventException;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SettlementRequestEvent(
        @NotBlank(message = "buyerCode 는 공백이거나 null일 수 없습니다.")
        String buyerCode,
        @NotBlank(message = "sellerCode 는 공백이거나 null일 수 없습니다.")
        String sellerCode,
        @NotBlank(message = "orderItemCode 는 공백이거나 null일 수 없습니다.")
        String orderItemCode,
        @NotNull(message = "settlementRate 는 필수입력값입니다.")
        @DecimalMin(value = "0.0", message = "settlementRate 는 0.0 이상이어야 합니다.")
        @DecimalMax(value = "1.0", message = "settlementRate 는 1.0 이하이어야 합니다.")
        BigDecimal settlementRate,
        @NotNull(message = "totalAmount 는 필수입력값입니다.")
        @PositiveOrZero(message = "totalAmount 는 0 이상이어야 합니다.")
        Long totalAmount
) {

    private static final BigDecimal DEFAULT_SETTLEMENT_RATE = new BigDecimal("0.3");

    public static SettlementRequestEvent of(String buyerCode, String sellerCode, String orderItemCode, String settlementRate, Long totalAmount) {
        if (settlementRate == null || settlementRate.isBlank()) {
            throw new InvalidSettlementEventException("settlementRate 는 null 또는 공백일 수 없습니다.");
        }
        try {
            return new SettlementRequestEvent(buyerCode, sellerCode, orderItemCode, new BigDecimal(settlementRate), totalAmount);
        } catch (NumberFormatException e) {
            throw new InvalidSettlementEventException("settlementRate 값이 올바른 숫자 형식이 아닙니다.");
        }
    }

    public static SettlementRequestEvent of(String buyerCode, String sellerCode, String orderItemCode, Long totalAmount) {
        return new SettlementRequestEvent(buyerCode, sellerCode, orderItemCode, DEFAULT_SETTLEMENT_RATE, totalAmount);
    }
}
