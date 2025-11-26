package com.ll.cart.model.vo.response;

import com.ll.cart.model.entity.CartItem;

public record CartItemAddResponse(
        String cartItemCode,
        Long productId,
        Integer quantity,
        Integer totalPrice
) {
    public static CartItemAddResponse from(CartItem cartItem) {
        return new CartItemAddResponse(
                cartItem.getCode(),
                cartItem.getProductId(),
                cartItem.getQuantity(),
                cartItem.getTotalPrice()
        );
    }
}

