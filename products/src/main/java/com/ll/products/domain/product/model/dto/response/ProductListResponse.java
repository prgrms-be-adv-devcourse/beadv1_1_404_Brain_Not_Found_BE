package com.ll.products.domain.product.model.dto.response;

import lombok.Builder;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;

import java.time.LocalDateTime;

@Builder
public record ProductListResponse(
        Long id,
        String code,
        String name,
        Long sellerId,
        String sellerName,
        Integer price,
        ProductStatus status,
        String mainImageUrl,
        LocalDateTime createdAt
) {
    public static ProductListResponse from(Product product) {
        String mainImageUrl = product.getImages().stream()
                .filter(image -> image.getIsMain())
                .findFirst()
                .map(image -> image.getUrl())
                .orElse(null);

        return ProductListResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .sellerId(product.getSellerId())
                .sellerName(product.getSellerName())
                .price(product.getPrice())
                .status(product.getStatus())
                .mainImageUrl(mainImageUrl)
                .createdAt(product.getCreatedAt())
                .build();
    }
}