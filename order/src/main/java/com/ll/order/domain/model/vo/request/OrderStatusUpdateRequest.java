package com.ll.order.domain.model.vo.request;

import com.ll.order.domain.model.enums.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "변경할 주문 상태가 필요합니다.")
        OrderStatus status
) {
}

