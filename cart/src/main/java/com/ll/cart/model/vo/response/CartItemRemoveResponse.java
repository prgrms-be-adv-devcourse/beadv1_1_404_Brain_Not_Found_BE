package com.ll.cart.model.vo.response;

public record CartItemRemoveResponse(
        String cartItemCode,
        Long productId,
        Integer quantity,
        Integer totalPrice
) {
}

