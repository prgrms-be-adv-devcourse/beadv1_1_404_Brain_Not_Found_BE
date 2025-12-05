package com.ll.products.domain.recommendation.document;

import com.ll.products.domain.product.model.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVectorDocument {

    private String productCode;
    private String name;
    private String description;
    private String categoryName;
    private Integer price;
    private String status;

    public static ProductVectorDocument from(Product product) {
        return ProductVectorDocument.builder()
                .productCode(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .price(product.getPrice())
                .status(product.getStatus().name())
                .build();
    }

    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productCode", productCode);
        payload.put("name", name);
        payload.put("description", description);
        payload.put("categoryName", categoryName);
        payload.put("price", price);
        payload.put("status", status);
        return payload;
    }

    public String generateEmbeddingText() {
        StringBuilder text = new StringBuilder();
        text.append("상품명: ").append(name).append(". ");
        if (description != null && !description.isBlank()) {
            text.append("설명: ").append(description).append(". ");
        }
        if (categoryName != null && !categoryName.isBlank()) {
            text.append("카테고리: ").append(categoryName).append(". ");
        }
        if (price != null) {
            text.append("가격: ").append(String.format("%,d원", price)).append(". ");
        }
        return text.toString();
    }
}