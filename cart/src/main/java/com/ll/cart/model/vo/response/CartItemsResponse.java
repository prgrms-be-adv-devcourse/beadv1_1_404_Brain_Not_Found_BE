package com.ll.cart.model.vo.response;

import com.ll.cart.model.entity.Cart;
import com.ll.cart.model.entity.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public record CartItemsResponse(
        String cartCode,
        Integer cartTotalPrice,
        List<CartItemInfo> items
) {
    public static CartItemsResponse from(Cart cart, List<CartItem> cartItems) {
        List<CartItemInfo> itemInfos = cartItems.stream()
                .map(CartItemInfo::from)
                .collect(Collectors.toList());

        return new CartItemsResponse(
                cart.getCode(),
                cart.getTotalPrice(),
                itemInfos
        );
    }
}


