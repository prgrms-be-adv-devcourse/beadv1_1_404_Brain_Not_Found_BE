package com.ll.order.domain.model.vo.response.cart;

public record CartItemInfo(
        String cartItemCode,
        Long productId,
        String productCode,
        Integer quantity,
        Integer totalPrice
) {}