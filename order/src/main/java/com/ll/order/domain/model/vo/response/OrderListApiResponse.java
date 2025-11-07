package com.ll.order.domain.model.vo.response;

import com.ll.order.domain.model.enums.OrderStatus;

import java.util.List;

public record OrderListApiResponse(
        OrderListBody body
) {
    public record OrderListBody(
            List<OrderInfo> orders,
            Integer page,
            Integer size
    ) {
    }

    public record OrderInfo(
            Long id,
            OrderStatus status,
            Integer totalPrice,
            UserInfo userInfo
    ) {
    }

    public record UserInfo(
            Long id,
            String address
    ) {
    }
}
