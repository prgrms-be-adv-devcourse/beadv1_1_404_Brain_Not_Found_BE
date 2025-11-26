package com.ll.order.domain.model.vo.response.cart;

import java.util.List;

public record CartItemsResponse(
        String cartCode,
        Integer cartTotalPrice,
        List<CartItemInfo> items
) {
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}

