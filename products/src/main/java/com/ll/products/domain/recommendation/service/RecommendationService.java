package com.ll.products.domain.recommendation.service;

import com.ll.products.domain.product.exception.ProductNotFoundException;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.domain.recommendation.document.ProductVectorDocument;
import com.ll.products.domain.recommendation.document.ProductVectorPoint;
import com.ll.products.domain.recommendation.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    private static final int BATCH_SIZE = 50;

    // 1. 유사 상품 추천
    @Transactional(readOnly = true)
    public List<RecommendationResponse> recommendSimilarProducts(String productCode, int limit) {
        Product product = findProductByCode(productCode);
        float[] embedding = generateProductEmbedding(product);
        List<RecommendationResponse> recommendations = vectorStoreService.searchSimilarProducts(embedding, limit + 1);
        return excludeSelfProduct(recommendations, productCode, limit);
    }

    // 2. 키워드 기반 상품 추천
    public List<RecommendationResponse> recommendProductsByKeyword(String keyword, int limit) {
        float[] embedding = embeddingService.generateEmbedding(keyword);
        return vectorStoreService.searchSimilarProducts(embedding, limit);
    }

    // 3. 상품 인덱스 삭제
    public void deleteProductIndex(String productCode) {
        try {
            vectorStoreService.deleteProductByCode(productCode);
            log.info("상품 색인 삭제 완료: {}", productCode);
        } catch (Exception e) {
            log.error("상품 색인 삭제 실패: {}", productCode, e);
        }
    }




    // 상품 인덱싱
    public void indexProduct(Product product) {
        try {
            ProductVectorPoint vectorPoint = createVectorPoint(product);
            vectorStoreService.upsertProduct(vectorPoint);
            log.info("상품 색인 완료: {}", product.getCode());
        } catch (Exception e) {
            log.error("상품 색인 실패: {}", product.getCode(), e);
        }
    }

    // 모든 상품 재색인
    public void reindexAllProducts() {
        vectorStoreService.recreateCollection();
        List<Product> products = productRepository.findAllByIsDeletedFalse();
        log.info("전체 상품 재색인 시작: 총 {}개", products.size());
        if (products.isEmpty()) {
            log.info("인덱싱할 상품이 없습니다.");
            return;
        }
        IndexingResult result = indexBatchProducts(products);
        log.info("전체 상품 재색인 완료: 성공 {}개, 실패 {}개", result.successCount, result.failCount);
    }

    // 상품 조회
    private Product findProductByCode(String productCode) {
        return productRepository.findByCodeAndIsDeletedFalse(productCode)
                .orElseThrow(() -> new ProductNotFoundException(productCode));
    }

    // 상품 임베딩
    private float[] generateProductEmbedding(Product product) {
        ProductVectorDocument document = ProductVectorDocument.from(product);
        String embeddingText = document.generateEmbeddingText();
        return embeddingService.generateEmbedding(embeddingText);
    }

    // 벡터 point 생성
    private ProductVectorPoint createVectorPoint(Product product) {
        ProductVectorDocument document = ProductVectorDocument.from(product);
        String embeddingText = document.generateEmbeddingText();
        float[] embedding = embeddingService.generateEmbedding(embeddingText);
        return ProductVectorPoint.of(document, embedding);
    }

    // 상품 리스트에서 본인 제외
    private List<RecommendationResponse> excludeSelfProduct(
            List<RecommendationResponse> recommendations,
            String productCode,
            int limit) {
        return recommendations.stream()
                .filter(rec -> !rec.productCode().equals(productCode))
                .limit(limit)
                .toList();
    }

    // 인덱싱(다중)
    private IndexingResult indexBatchProducts(List<Product> products) {
        int successCount = 0;
        int failCount = 0;

        for (int startIndex = 0; startIndex < products.size(); startIndex += BATCH_SIZE) {
            int endIndex = Math.min(startIndex + BATCH_SIZE, products.size());
            List<Product> batch = products.subList(startIndex, endIndex);
            log.info("배치 처리 중: {}-{}/{}", startIndex + 1, endIndex, products.size());
            try {
                indexProducts(batch);
                successCount += batch.size();
                log.info("배치 처리 완료: {} 건", batch.size());
            } catch (Exception e) {
                log.error("배치 처리 실패: {}-{}", startIndex + 1, endIndex, e);
                int[] fallbackResult = fallbackIndexing(batch);
                successCount += fallbackResult[0];
                failCount += fallbackResult[1];
            }
        }
        return new IndexingResult(successCount, failCount);
    }

    // 상품 저장(다중)
    private void indexProducts(List<Product> batch) {
        // vectorDocument 생성
        List<ProductVectorDocument> documents = batch.stream()
                .map(ProductVectorDocument::from)
                .toList();

        // 임베딩 텍스트 생성
        List<String> embeddingTexts = documents.stream()
                .map(ProductVectorDocument::generateEmbeddingText)
                .toList();

        // 배치 임베딩 생성
        List<float[]> embeddings = embeddingService.generateEmbeddings(embeddingTexts);

        // point 리스트 생성
        List<ProductVectorPoint> vectorPoints = IntStream.range(0, documents.size())
                .mapToObj(i -> ProductVectorPoint.of(documents.get(i), embeddings.get(i)))
                .toList();

        // Qdrant에 배치 저장
        vectorStoreService.upsertProducts(vectorPoints);
    }

    // 배치 실패 시 개별 인덱싱
    private int[] fallbackIndexing(List<Product> batch) {
        int success = 0;
        int fail = 0;

        for (Product product : batch) {
            try {
                indexProduct(product);
                success++;
            } catch (Exception e) {
                log.error("상품 색인 실패: {}", product.getCode(), e);
                fail++;
            }
        }
        return new int[]{success, fail};
    }

    // 인덱싱 결과 record
    private record IndexingResult(int successCount, int failCount) {}
}