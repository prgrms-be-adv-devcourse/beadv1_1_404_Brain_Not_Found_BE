package com.ll.products.domain.recommendation.config;

import com.ll.products.domain.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!prod")
@Order(99)
@RequiredArgsConstructor
public class VectorDbIndexInitializer implements ApplicationRunner {
    private final RecommendationService recommendationService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.debug("=== VectorDB 전체 재색인 시작 (백그라운드) ===");

        try {
            long startTime = System.currentTimeMillis();
            recommendationService.reindexAllProducts();
            long duration = System.currentTimeMillis() - startTime;
            log.debug("=== VectorDB 전체 재색인 완료 (소요시간: {}ms) ===", duration);
        } catch (Exception e) {
            log.error("=== VectorDB 재색인 실패: {} ===", e.getMessage(), e);
        }
    }
}
