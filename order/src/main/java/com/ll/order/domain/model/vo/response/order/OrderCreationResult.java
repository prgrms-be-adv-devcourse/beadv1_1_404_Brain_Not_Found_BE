package com.ll.order.domain.model.vo.response.order;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;

import java.util.List;

public record OrderCreationResult(
        Order order,
        List<OrderItem> orderItems
) {
}

