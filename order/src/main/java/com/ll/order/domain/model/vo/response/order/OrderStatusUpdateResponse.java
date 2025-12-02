package com.ll.order.domain.model.vo.response.order;

import com.ll.order.domain.model.enums.order.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusUpdateResponse(
        String orderCode,
        OrderStatus status,
        LocalDateTime updatedAt
) {
}

