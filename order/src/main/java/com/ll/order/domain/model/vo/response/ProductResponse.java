package com.ll.order.domain.model.vo.response;

public record ProductResponse(
        Long productId,
        Long sellerId,
        String productName,
        int quantity,
        int totalPrice,
        String image
) {
}


