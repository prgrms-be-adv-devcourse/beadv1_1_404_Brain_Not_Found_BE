package com.ll.core.model.vo.kafka;

import com.ll.core.model.exception.InvalidSettlementEventException;
import com.ll.core.model.vo.kafka.enums.OrderEventType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record OrderEvent(
        @NotNull(message = "orderEventType 은 필수입력값입니다.")
        OrderEventType orderEventType,
        @NotBlank(message = "buyerCode 는 공백이거나 null일 수 없습니다.")
        String buyerCode,
        @NotBlank(message = "sellerCode 는 공백이거나 null일 수 없습니다.")
        String sellerCode,
        @NotBlank(message = "orderItemCode 는 공백이거나 null일 수 없습니다.")
        String orderItemCode,
        @NotBlank(message = "referenceCode 는 공백이거나 null일 수 없습니다.")
        String referenceCode,
        @NotNull(message = "settlementRate 는 필수입력값입니다.")
        @DecimalMin(value = "0.0", message = "settlementRate 는 0.0 이상이어야 합니다.")
        @DecimalMax(value = "1.0", message = "settlementRate 는 1.0 이하이어야 합니다.")
        BigDecimal settlementRate,
        @NotNull(message = "amount 는 필수입력값입니다.")
        @PositiveOrZero(message = "amount 는 0 이상이어야 합니다.")
        Long amount
) {

    private static final BigDecimal DEFAULT_SETTLEMENT_RATE = new BigDecimal("0.3");

    public static OrderEvent of(OrderEventType orderEventType,String buyerCode, String sellerCode, String orderItemCode, String referenceCode, String settlementRate, Long amount) {
        if (settlementRate == null || settlementRate.isBlank()) {
            throw new InvalidSettlementEventException("settlementRate 는 null 또는 공백일 수 없습니다.");
        }
        try {
            return new OrderEvent(orderEventType, buyerCode, sellerCode, orderItemCode, referenceCode, new BigDecimal(settlementRate), amount);
        } catch (NumberFormatException e) {
            throw new InvalidSettlementEventException("settlementRate 값이 올바른 숫자 형식이 아닙니다.");
        }
    }

    public static OrderEvent of(OrderEventType orderEventType,String buyerCode, String sellerCode, String orderItemCode, String referenceCode, Long amount) {
        return new OrderEvent(orderEventType, buyerCode, sellerCode, orderItemCode, referenceCode, DEFAULT_SETTLEMENT_RATE, amount);
    }
}
