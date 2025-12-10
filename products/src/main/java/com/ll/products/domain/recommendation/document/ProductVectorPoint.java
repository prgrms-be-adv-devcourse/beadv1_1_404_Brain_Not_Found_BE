package com.ll.products.domain.recommendation.document;

import lombok.Getter;

@Getter
public class ProductVectorPoint {

    private final ProductVectorDocument document;
    private final float[] embedding;

    public static ProductVectorPoint of(ProductVectorDocument document, float[] embedding) {
        return new ProductVectorPoint(document, embedding);
    }

    private ProductVectorPoint(ProductVectorDocument document, float[] embedding) {
        if (document == null) {
            throw new IllegalArgumentException("document는 null일 수 없습니다");
        }
        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("embedding은 null이거나 비어있을 수 없습니다");
        }
        this.document = document;
        this.embedding = embedding;
    }
}