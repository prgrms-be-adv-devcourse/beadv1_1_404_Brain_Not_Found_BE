package com.ll.cart.model.vo.response;

public record CartItemAddResponse(
        String cartItemCode,
        Long productId,
        Integer quantity,
        Integer totalPrice
) {
}

