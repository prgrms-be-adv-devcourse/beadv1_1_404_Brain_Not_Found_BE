package com.ll.order.domain.model.vo.response;

import com.ll.order.domain.model.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusUpdateResponse(
        String orderCode,
        OrderStatus status,
        LocalDateTime updatedAt
) {
}

