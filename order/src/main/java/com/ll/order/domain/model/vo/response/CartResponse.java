package com.ll.order.domain.model.vo.response;

import java.util.List;

public record CartResponse(
        Long cartId,
        String cartCode,
        Long userId,
        List<CartItemResponse> items,
        Integer totalPrice
) {
    public record CartItemResponse(
            Long productId,
            String productCode,
            Integer quantity,
            Integer price
    ) {
    }
}

