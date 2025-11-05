package com.ll.order.domain.model.vo.response;

import com.ll.order.domain.model.enums.OrderStatus;

public record OrderDetailResponse(
        Long orderId,
        OrderStatus status,
        int totalPrice,
        UserInfo userInfo
) {
    public record UserInfo(
            Long userId,
            String address
    ) {
    }
}
