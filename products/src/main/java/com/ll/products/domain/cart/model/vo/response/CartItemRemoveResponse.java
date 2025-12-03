package com.ll.products.domain.cart.model.vo.response;

import com.ll.products.domain.cart.model.entity.CartItem;

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

