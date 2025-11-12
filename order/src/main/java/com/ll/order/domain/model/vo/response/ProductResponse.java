package com.ll.order.domain.model.vo.response;

import com.ll.order.domain.model.enums.ProductSaleStatus;

public record ProductResponse(
        Long productId,
        Long sellerId,
        String productName,
        int quantity,
        int totalPrice,
        ProductSaleStatus saleStatus,
        String image
) {
}


