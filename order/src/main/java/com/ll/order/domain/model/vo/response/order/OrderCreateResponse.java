package com.ll.order.domain.model.vo.response.order;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.order.OrderStatus;
import com.ll.order.domain.model.enums.order.OrderType;

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
    public static OrderCreateResponse from(Order order, List<OrderItem> orderItems) {
        return new OrderCreateResponse(
                order.getId(),
                order.getCode(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                order.getOrderType(),
                order.getAddress(),
                order.getBuyerId(),
                orderItems.stream()
                        .map(OrderItemInfo::from)
                        .toList()
        );
    }

    public record OrderItemInfo(
            Long id,
            String orderItemCode,
            Long productId,
            String sellerCode,
            String productName,
            Integer quantity,
            Integer price
    ) {
        public static OrderItemInfo from(OrderItem orderItem) {
            return new OrderItemInfo(
                    orderItem.getId(),
                    orderItem.getCode(),
                    orderItem.getProductId(),
                    orderItem.getSellerCode(),
                    orderItem.getProductName(),
                    orderItem.getQuantity(),
                    orderItem.getPrice()
            );
        }
    }
}

