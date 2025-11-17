package com.ll.products.domain.search.dto;

import com.ll.products.domain.search.document.ProductDocument;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProductSearchResponse(
        String code,
        String name,
        String description,
        Integer price,
        Integer quantity,
        String status,
        String categoryName,
        String mainImageUrl,
        LocalDateTime createdAt
) {
    public static ProductSearchResponse from(ProductDocument document) {
        return ProductSearchResponse.builder()
                .code(document.getCode())
                .name(document.getName())
                .description(document.getDescription())
                .price(document.getPrice())
                .quantity(document.getQuantity())
                .status(document.getStatus())
                .categoryName(document.getCategoryName())
                .mainImageUrl(document.getMainImageUrl())
                .createdAt(document.getCreatedAt())
                .build();
    }
}