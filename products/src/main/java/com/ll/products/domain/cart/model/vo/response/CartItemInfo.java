package com.ll.products.domain.cart.model.vo.response;

import com.ll.products.domain.cart.model.entity.CartItem;

public record CartItemInfo(
        String cartItemCode,
        Long productId,
        String productCode,
        Integer quantity,
        Integer totalPrice
) {
    public static CartItemInfo from(CartItem cartItem, String productCode) {
        return new CartItemInfo(
                cartItem.getCode(),
                cartItem.getProductId(),
                productCode,
                cartItem.getQuantity(),
                cartItem.getTotalPrice()
        );
    }
}


