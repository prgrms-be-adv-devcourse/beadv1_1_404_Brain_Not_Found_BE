package com.ll.cart.model.vo.response;

import java.util.List;

public record CartItemsResponse(
        String cartCode,
        Integer cartTotalPrice,
        List<CartItemInfo> items
) {}


