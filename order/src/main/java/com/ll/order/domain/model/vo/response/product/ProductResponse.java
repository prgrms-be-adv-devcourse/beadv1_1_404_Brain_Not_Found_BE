package com.ll.order.domain.model.vo.response.product;

import com.ll.order.domain.model.enums.product.ProductStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ProductResponse(
        Long id,
        String code,
        String name,
        Long categoryId,
        String categoryName,
        String sellerCode,
        String sellerName,
        ProductStatus status,
        Integer quantity,
        String description,
        Integer price,
        List<ProductImageDto> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

