package com.ll.cart.model.vo.response.cart;

import com.ll.cart.model.entity.CartItem;

public record CartItemRemoveResponse(
        String cartItemCode,
        Long productId,
        Integer quantity,
        Integer totalPrice
) {
    public static CartItemRemoveResponse from(CartItem cartItem) {
        return new CartItemRemoveResponse(
                cartItem.getCode(),
                cartItem.getProductId(),
                cartItem.getQuantity(),
                cartItem.getTotalPrice()
        );
    }
}

