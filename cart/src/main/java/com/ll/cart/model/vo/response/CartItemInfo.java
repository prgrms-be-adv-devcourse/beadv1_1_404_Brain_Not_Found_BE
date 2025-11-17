package com.ll.cart.model.vo.response;

public record CartItemInfo(
        String cartItemCode,
        Long productId,
        Integer quantity,
        Integer totalPrice
) {}


