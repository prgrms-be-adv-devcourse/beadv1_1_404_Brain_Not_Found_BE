package com.ll.order.domain.model.vo.response.order;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.enums.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public record OrderPageResponse(
        List<OrderInfo> orders,
        PageInfo pageInfo
) {
    public static OrderPageResponse from(Page<Order> orderPage) {
        return new OrderPageResponse(
                orderPage.getContent().stream()
                        .map(OrderInfo::from)
                        .toList(),
                PageInfo.from(orderPage)
        );
    }

    public record OrderInfo(
            Long id,
            String orderCode,
            OrderStatus status,
            Integer totalPrice,
            UserInfo userInfo
    ) {
        public static OrderInfo from(Order order) {
            return new OrderInfo(
                    order.getId(),
                    order.getCode(),
                    order.getOrderStatus(),
                    order.getTotalPrice(),
                    UserInfo.from(order)
            );
        }
    }

    public record UserInfo(
            Long id,
            String address
    ) {
        public static UserInfo from(Order order) {
            return new UserInfo(
                    order.getBuyerId(),
                    order.getAddress()
            );
        }
    }

    public record PageInfo(
            int currentPage,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean hasPrevious
    ) {
        public static PageInfo from(Page<?> page) {
            return new PageInfo(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.hasNext(),
                    page.hasPrevious()
            );
        }
    }
}

