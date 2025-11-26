package com.ll.order.domain.model.vo.response.order;

import com.ll.order.domain.model.entity.Order;
import com.ll.order.domain.model.entity.OrderItem;
import com.ll.order.domain.model.enums.OrderStatus;
import com.ll.order.domain.model.vo.response.product.ProductResponse;

import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        OrderStatus status,
        int totalPrice,
        UserInfo userInfo,
        List<ItemInfo> items
) {
    public static OrderDetailResponse from(Order order, List<ItemInfo> items) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderStatus(),
                order.getTotalPrice(),
                UserInfo.from(order),
                items
        );
    }

    public record UserInfo(
            Long userId,
            String address
    ) {
        public static UserInfo from(Order order) {
            return new UserInfo(
                    order.getBuyerId(),
                    order.getAddress()
            );
        }
    }

    public record ItemInfo(
            String orderItemCode,
            Long productId,
            String sellerCode,
            String productName,
            int quantity,
            int price,
            String productImage
    ) {
        public static ItemInfo from(OrderItem orderItem, ProductResponse product) {
            String productImage = product.images() != null && !product.images().isEmpty()
                    ? product.images().get(0).url()
                    : null;

            return new ItemInfo(
                    orderItem.getCode(),
                    orderItem.getProductId(),
                    orderItem.getSellerCode(),
                    orderItem.getProductName(),
                    orderItem.getQuantity(),
                    orderItem.getPrice(),
                    productImage
            );
        }
    }
}
