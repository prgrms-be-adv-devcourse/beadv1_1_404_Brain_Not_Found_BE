package com.ll.products.domain.recommendation.service;

import com.ll.core.model.vo.kafka.ProductEvent;
import com.ll.products.domain.recommendation.document.ProductVectorDocument;
import com.ll.products.domain.recommendation.document.ProductVectorPoint;
import com.ll.products.domain.recommendation.exception.VectorIndexException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorIndexService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public void indexProduct(ProductEvent event) {
        try {
            ProductVectorDocument document = convertToProductVector(event);
            String embeddingText = document.generateEmbeddingText();
            log.debug("임베딩 텍스트 생성: {}", embeddingText);
            float[] embedding = embeddingService.generateEmbedding(embeddingText);
            ProductVectorPoint point = ProductVectorPoint.of(document, embedding);
            vectorStoreService.upsertProduct(point);
            log.info("벡터 인덱싱 완료: productCode={}", event.productCode());

        } catch (Exception e) {
            log.error("벡터 인덱싱 실패: productCode={}, error={}", event.productCode(), e.getMessage(), e);
            throw new VectorIndexException("벡터 인덱싱에 실패했습니다.");
        }
    }


    // 상품 코드로 벡터db 데이터 삭제
    public void deleteProductIndex(String productCode) {
        try {
            vectorStoreService.deleteProductByCode(productCode);
            log.info("벡터 인덱스 삭제 완료: productCode={}", productCode);
        } catch (Exception e) {
            log.error("벡터 인덱스 삭제 실패: productCode={}, error={}", productCode, e.getMessage(), e);
            throw new VectorIndexException("벡터 인덱스 삭제에 실패했습니다.");
        }
    }

    // event -> document 전환
    private ProductVectorDocument convertToProductVector(ProductEvent event) {
        return ProductVectorDocument.builder()
                .productCode(event.productCode())
                .name(event.name())
                .description(event.description())
                .categoryName(event.categoryName())
                .price(event.price())
                .status(event.status())
                .build();
    }
}