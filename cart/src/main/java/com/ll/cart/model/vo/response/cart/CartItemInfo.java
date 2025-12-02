package com.ll.cart.model.vo.response.cart;

import com.ll.cart.model.entity.CartItem;

public record CartItemInfo(
        String cartItemCode,
        Long productId,
        Integer quantity,
        Integer totalPrice
) {
    public static CartItemInfo from(CartItem cartItem) {
        return new CartItemInfo(
                cartItem.getCode(),
                cartItem.getProductId(),
                cartItem.getQuantity(),
                cartItem.getTotalPrice()
        );
    }
}


