package com.ll.order.domain.model.vo.response;

import com.ll.order.domain.model.enums.OrderStatus;

import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        OrderStatus status,
        int totalPrice,
        UserInfo userInfo,
        List<ItemInfo> items
) {
    public record UserInfo(
            Long userId,
            String address
    ) {
    }

    public record ItemInfo(
            String orderItemCode,
            Long productId,
            Long sellerId,
            String productName,
            int quantity,
            int price,
            String productImage
    ) {
    }
}
