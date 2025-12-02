package com.ll.products.domain.search.config;

import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.domain.search.document.ProductDocument;
import com.ll.products.domain.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 앱 실행 시, 인덱스 삭제 및 재생성(개발 환경에서만 활성화)
@Slf4j
@Component
@Profile("local")
@Order(100)
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Elasticsearch 전체 재색인 시작 ===");

        try {
            reindexAll();
        } catch (Exception e) {
            log.error("Elasticsearch 재색인 실패: {}", e.getMessage(), e);
        }
        log.info("=== Elasticsearch 전체 재색인 종료 ===");
    }


    @Transactional(readOnly = true)
    public void reindexAll() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);

        // 기존 인덱스 삭제
        if (indexOps.exists()) {
            indexOps.delete();
            log.info("기존 Elasticsearch 인덱스 삭제 완료");
        }

        // 새로운 인덱스 생성
        indexOps.create();
        indexOps.putMapping(indexOps.createMapping());
        log.info("새로운 Elasticsearch 인덱스 생성 완료");

        // 모든 상품 조회
        List<Product> products = productRepository.findAllByIsDeletedFalse();
        log.info("MySQL에서 조회된 상품 수: {}", products.size());

        // ProductDocument로 변환
        List<ProductDocument> documents = products.stream()
                .map(ProductDocument::from)
                .toList();

        // es에 저장
        productSearchRepository.saveAll(documents);
        log.info("Elasticsearch 재색인 완료: {} 건", documents.size());
    }
}
