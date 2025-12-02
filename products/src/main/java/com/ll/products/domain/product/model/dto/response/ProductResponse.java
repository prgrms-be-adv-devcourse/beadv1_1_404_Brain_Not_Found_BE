package com.ll.products.domain.product.model.dto.response;

import lombok.Builder;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;

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
        List<ProductImageResponse> images,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product, String s3BaseUrl) {
        return ProductResponse.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .sellerCode(product.getSellerCode())
                .sellerName(product.getSellerName())
                .quantity(product.getQuantity())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus())
                .images(product.getImages().stream()
                        .map(image -> ProductImageResponse.from(image, s3BaseUrl))
                        .toList())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
