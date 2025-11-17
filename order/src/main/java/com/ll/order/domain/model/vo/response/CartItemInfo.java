package com.ll.order.domain.model.vo.response;

public record CartItemInfo(
        String cartItemCode,
        Long productId,
        Integer quantity,
        Integer totalPrice
) {}

