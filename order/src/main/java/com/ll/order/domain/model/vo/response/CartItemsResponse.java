package com.ll.order.domain.model.vo.response;

import java.util.List;

public record CartItemsResponse(
        String cartCode,
        Integer cartTotalPrice,
        List<CartItemInfo> items
) {}

