package com.ll.products.domain.recommendation.controller;

import com.ll.products.domain.recommendation.controller.swagger.*;
import com.ll.products.domain.recommendation.dto.RecommendationResponse;
import com.ll.products.domain.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Recommendation", description = "상품 추천 API")
@Slf4j
@RestController
@RequestMapping("/api/products/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    // 1. 유사한 상품 추천
    @GetMapping("/similar/{productCode}")
    @SimilarProductsApiResponse
    public ResponseEntity<List<RecommendationResponse>> getSimilarProducts(
            @PathVariable String productCode,
            @RequestParam(defaultValue = "10") int limit
    ) {
        long startTime = System.currentTimeMillis();
        log.info("유사 상품 추천 요청: productCode={}, limit={}", productCode, limit);
        List<RecommendationResponse> recommendations = recommendationService.recommendSimilarProducts(productCode, limit);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("상품 코드 기반 추천 완료: productCode={}, 결과 개수={}, 소요시간={}ms", productCode, recommendations.size(), duration);
        return ResponseEntity.ok(recommendations);
    }

    // 2. 검색어 기반 상품 추천
    @GetMapping("/search")
    @SearchRecommendationsApiResponse
    public ResponseEntity<List<RecommendationResponse>> searchRecommendations(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit
    ) {
        long startTime = System.currentTimeMillis();
        log.info("검색어 기반 추천 요청: keyword={}, limit={}", keyword, limit);
        List<RecommendationResponse> recommendations = recommendationService.recommendProductsByKeyword(keyword, limit);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("검색어 기반 추천 완료: keyword={}, 결과 개수={}, 소요시간={}ms", keyword, recommendations.size(), duration);

        return ResponseEntity.ok(recommendations);
    }

    // 3. 유저 상세 조회 기록 기반 상품 추천
    @GetMapping("/view-history")
    @ViewHistoryRecommendationsApiResponse
    public ResponseEntity<List<RecommendationResponse>> recommendationsFromViewHistory(
            @RequestHeader("X-User-Code") String userCode,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<RecommendationResponse> recommendations = recommendationService.recommendProductsByViewHistory(userCode, limit);
        return ResponseEntity.ok(recommendations);
    }

    // 4. 유저 검색 기록 기반 상품 추천
    @GetMapping("/search-history")
    @SearchHistoryRecommendationsApiResponse
    public ResponseEntity<List<RecommendationResponse>> recommendationsFromSearchHistory(
            @RequestHeader("X-User-Code") String userCode,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<RecommendationResponse> recommendations = recommendationService.recommendProductsBySearchHistory(userCode, limit);
        return ResponseEntity.ok(recommendations);
    }

    // 5. 전체 상품 재색인 (관리자용)
    @PostMapping("/reindex")
    @ReindexAllProductsApiResponse
    public ResponseEntity<String> reindexAllProducts(
            @RequestHeader("X-Role") String role) {
        log.info("전체 상품 재색인 요청");
        recommendationService.reindexAllProducts(role);
        return ResponseEntity.ok("전체 상품 재색인이 완료되었습니다.");
    }

    // 6. 상세 조회 기록 및 llm 기반
    @GetMapping("/view-history/llm")
    @LlmRecommendationsApiResponse
    public ResponseEntity<List<RecommendationResponse>> recommendationsWithLlm(
            @RequestHeader("X-User-Code") String userCode,
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<RecommendationResponse> recommendations = recommendationService.recommendProductsByLlm(userCode, limit);
        return ResponseEntity.ok(recommendations);
    }

}