package com.ll.products.domain.search.dto;

import com.ll.products.domain.search.document.ProductDocument;
import com.ll.products.global.util.S3ImageUrlBuilder;
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
    public static ProductSearchResponse from(ProductDocument document, String s3BaseUrl) {
        String mainImageUrl = S3ImageUrlBuilder.buildImageUrl(document.getMainImageFileKey(), s3BaseUrl);

        return ProductSearchResponse.builder()
                .code(document.getCode())
                .name(document.getName())
                .description(document.getDescription())
                .price(document.getPrice())
                .quantity(document.getQuantity())
                .status(document.getStatus())
                .categoryName(document.getCategoryName())
                .mainImageUrl(mainImageUrl)
                .createdAt(document.getCreatedAt())
                .build();
    }
}