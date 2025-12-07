package com.ll.products.domain.recommendation.dto;

import lombok.Builder;

@Builder
public record RecommendationResponse(
        String productCode,
        String name,
        String description,
        String categoryName,
        Integer price,
        String status,
        Float score
) {
}