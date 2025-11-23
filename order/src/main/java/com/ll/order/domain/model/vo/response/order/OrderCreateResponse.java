package com.ll.order.domain.model.vo.response.order;

import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.enums.OrderType;

import java.util.List;

public record OrderCreateResponse(
        Long id,
        String orderCode,
        OrderStatus orderStatus,
        Integer totalPrice,
        OrderType orderType,
        String address,
        Long buyerId,
        List<OrderItemInfo> orderItems
) {
    public record OrderItemInfo(
            Long id,
            String orderItemCode,
            Long productId,
            String sellerCode,
            String productName,
            Integer quantity,
            Integer price
    ) {
    }
}

