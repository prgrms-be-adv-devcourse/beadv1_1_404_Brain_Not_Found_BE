package com.ll.order.domain.model.vo.response;

import java.util.List;

// record 안에 record 클래스는 어떤 의도일까요?
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

