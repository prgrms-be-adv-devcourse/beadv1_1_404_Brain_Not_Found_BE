package com.ll.cart.dto.response;

public record CartItemAddResponse(
        String cartItemCode,
        Long productId,
        Integer quantity,
        Integer totalPrice
) {
}

