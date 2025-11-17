package com.ll.core.model.vo.kafka;

import com.ll.core.model.vo.kafka.enums.RefundEventType;
import jakarta.validation.constraints.*;

public record RefundEvent (
        @NotNull(message = "refundEventType 은 필수입력값입니다.")
        RefundEventType refundEventType,
        @NotBlank(message = "buyerCode 는 공백이거나 null일 수 없습니다.")
        String buyerCode,
        @NotBlank(message = "sellerCode 는 공백이거나 null일 수 없습니다.")
        String sellerCode,
        @NotBlank(message = "orderItemCode 는 공백이거나 null일 수 없습니다.")
        String orderItemCode,
        @NotBlank(message = "referenceCode 는 공백이거나 null일 수 없습니다.")
        String referenceCode,
        @NotNull(message = "amount 는 필수입력값입니다.")
        @PositiveOrZero(message = "amount 는 0 이상이어야 합니다.")
        Long amount
) {
}
