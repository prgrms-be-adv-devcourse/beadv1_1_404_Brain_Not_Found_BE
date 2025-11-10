package com.ll.order.domain.model.vo.response;

import com.ll.order.domain.model.enums.OrderStatus;

import java.util.List;

public record OrderPageResponse(
        List<OrderInfo> orders,
        PageInfo pageInfo
) {
    public record OrderInfo(
            Long id,
            String orderCode,
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

    public record PageInfo(
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {
    }
}

