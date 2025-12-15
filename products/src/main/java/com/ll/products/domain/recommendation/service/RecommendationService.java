package com.ll.products.domain.recommendation.service;

import com.ll.products.domain.history.service.HistoryFacadeService;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.domain.recommendation.document.ProductVectorDocument;
import com.ll.products.domain.recommendation.document.ProductVectorPoint;
import com.ll.products.domain.recommendation.dto.RecommendationResponse;
import com.ll.products.domain.recommendation.exception.VectorNotFoundException;
import io.qdrant.client.grpc.JsonWithInt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final HistoryFacadeService historyFacadeService;

    private static final int BATCH_SIZE = 300;
    private final ChatClient chatClient;

    // 1. 유사 상품 추천
    public List<RecommendationResponse> recommendSimilarProducts(String productCode, int limit) {
        float[] embedding = vectorStoreService.getVectorByProductCode(productCode);
        List<RecommendationResponse> recommendations = vectorStoreService.searchSimilarProducts(embedding, limit + 1);
        return excludeSelfProduct(recommendations, productCode, limit);
    }

    // 2. 키워드 기반 상품 추천
    public List<RecommendationResponse> recommendProductsByKeyword(String keyword, int limit) {
        float[] embedding = embeddingService.generateEmbedding(keyword);
        return vectorStoreService.searchSimilarProducts(embedding, limit);
    }

    // 3. 유저 상세 조회 기록 기반 상품 추천
    public List<RecommendationResponse> recommendProductsByViewHistory(String userCode, int limit) {
        List<String> viewList = historyFacadeService.getViewList(userCode);
        if (viewList == null || viewList.isEmpty()) {
            log.info("유저 조회 기록이 없음: userCode={}", userCode);
            return List.of();
        }
        float[] averageEmbedding = getAverageEmbedding(viewList);
        List<RecommendationResponse> recommendations = vectorStoreService.searchSimilarProducts(averageEmbedding, limit + viewList.size());
        return excludeViewedProducts(recommendations, viewList, limit);
    }

    // 4. 유저 검색 기록 기반 상품 추천
    public List<RecommendationResponse> recommendProductsBySearchHistory(String userCode, int limit) {
        List<String> searchList = historyFacadeService.getSearchList(userCode);
        if (searchList == null || searchList.isEmpty()) {
            log.info("유저 검색 기록이 없음: userCode={}", userCode);
            return List.of();
        }
        String keywords = String.join(" ", searchList);
        float[] embedding = embeddingService.generateEmbedding(keywords);
        return vectorStoreService.searchSimilarProducts(embedding, limit);
    }

    // 5. 상세 조회 목록 기반 상품 추천(with llm)
    public List<RecommendationResponse> recommendProductsByLlm(String userCode, int limit) {
        List<String> viewList = historyFacadeService.getViewList(userCode);
        if (viewList == null || viewList.isEmpty()) {
            log.info("유저 조회 기록이 없음: userCode={}", userCode);
            return List.of();
        }
        String llmResult = generateSearchQuery(generateProductsText(viewList));
        float[] embeddingResult = embeddingService.generateEmbedding(llmResult);
        List<RecommendationResponse> recommendations = vectorStoreService.searchSimilarProducts(embeddingResult, limit + viewList.size());
        return excludeViewedProducts(recommendations, viewList, limit);
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
        List<Product> products = productRepository.findAllByIsDeletedFalseAndStatus(ProductStatus.ON_SALE);
        log.info("전체 상품 재색인 시작: 총 {}개", products.size());
        if (products.isEmpty()) {
            log.info("인덱싱할 상품이 없습니다.");
            return;
        }
        IndexingResult result = indexBatchProducts(products);
        log.info("전체 상품 재색인 완료: 성공 {}개, 실패 {}개", result.successCount, result.failCount);
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

    // 조회 상품 벡터 평균 계산
    private float[] getAverageEmbedding(List<String> productCodes) {
        List<float[]> embeddings = productCodes.stream()
                .map(code -> {
                    try {
                        return vectorStoreService.getVectorByProductCode(code);
                    } catch (Exception e) {
                        log.warn("벡터 조회 실패, 건너뜀: productCode={}", code);
                        return null;
                    }
                })
                .filter(embedding -> embedding != null)
                .toList();
        if (embeddings.isEmpty()) {
            throw new VectorNotFoundException();
        }
        int vectorSize = embeddings.getFirst().length;
        float[] average = new float[vectorSize];

        for (float[] embedding : embeddings) {
            for (int i = 0; i < vectorSize; i++) {
                average[i] += embedding[i];
            }
        }

        for (int i = 0; i < vectorSize; i++) {
            average[i] /= embeddings.size();
        }
        return average;
    }

    // 이미 조회한 상품 제외
    private List<RecommendationResponse> excludeViewedProducts(
            List<RecommendationResponse> recommendations,
            List<String> viewedProductCodes,
            int limit) {
        return recommendations.stream()
                .filter(rec -> !viewedProductCodes.contains(rec.productCode()))
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

    // 상품코드 목록으로 text 생성
    public String generateProductsText(List<String> productCodes) {
        StringBuilder text = new StringBuilder();
        for (String productCode : productCodes) {
            Map<String, JsonWithInt.Value> payloadMap = vectorStoreService.getPayloadByProductCode(productCode);
            if(!payloadMap.isEmpty()) {
                text.append(String.format("상품명: %s, 카테고리명: %s, 설명: %s, 가격: %d.\n",
                        payloadMap.get("name").getStringValue(),
                        payloadMap.get("categoryName").getStringValue(),
                        payloadMap.get("description").getStringValue(),
                        payloadMap.get("price").getIntegerValue()
                ));
            }
        }
        return text.toString();
    }

    // llm
    private String generateSearchQuery(String viewSummary) {
        String prompt = """
                당신은 굿즈(애니메이션/스포츠/아이돌/연예인/게임 등) 전문 상품 추천 전문가입니다. \s
                사용자의 조회 상품 목록을 분석하여 추가 구매 가능성이 높은 굿즈를 추천해주세요.
                
                %s
                
                위 상품 목록을 기반으로 아래 조건을 만족하는 추천 상품 정보를 한 줄로 출력하세요.
                
                [필수 조건]
                1. 반드시 굿즈 상품만 추천해야 합니다.
                2. 출력 텍스트는 임베딩 후 벡터DB에서 유사도 검색에 사용될 예정이므로, 상품명·카테고리명·상품설명이 명확히 포함되어야 합니다.
                3. 벡터DB는 ‘상품명 / 카테고리명 / 상품설명’ 기준으로 임베딩되어 저장됩니다.
                4. 단순히 유사 카테고리를 추천하는 것이 아니라, **구체적인 개별 상품**을 추천해야 합니다.
                5. 가격 정보를 출력할 필요는 없지만, 사용자 관심 상품들과 가격대가 지나치게 다른 상품은 추천하지 마세요.
                6. 전체 문장은 반드시 **50자 이내**로 작성해야 합니다.
                7. 필요하다면 유사도 검색에 도움될 정보를 추가 포함해도 되며, 간단한 추천 사유도 포함하세요.
                8. (중요) 단순 동일 카테고리 유사품이 아니라, **해당 '카테고리 특성' 기반의 ‘확장 관심사’ 상품**을 추천해야 합니다.
                   - 8-1. 애니메이션: 한 시리즈를 좋아하면 다른 시리즈도 좋아할 가능성이 높음
                         예) 원피스 굿즈 조회 -> 나루토 굿즈 추천 (가능)
                   - 8-2. 스포츠: 한 종목을 좋아하면 그 종목에 집중하는 경향이 있음. 한 팀을 좋아하면 해당 팀에 집중하는 경향이 있음.
                         예) 축구 굿즈 조회 -> 농구·야구 굿즈 추천 (금지)
                         예) 첼시FC 굿즈 조회 -> 맨체스터 유나이티드 굿즈 추천 (금지)
                   - 8-3. 영화: 특정 장르의 영화를 좋아하면 유사한 장르의 다른 영화도 좋아할 가능성 높음
                         예) 인터스텔라 굿즈 조회 -> 마션 굿즈 추천 (가능)
                         예) 인터스텔라 굿즈 조회 -> 라라랜드 굿즈 추천 (금지)
                   - 8-4. 아이돌: 특정 아이돌을 좋아하면 해당 아이돌에 집중하는 경향이 있음
                         예) 뉴진스 굿즈 조회 -> 에스파 굿즈 추천 (금지)
                         예) 뉴진스 굿즈 조회 -> 뉴진스 다른 굿즈 추천 (가능)
                   - 8-5. 게임: 특정 장르의 게임을 좋아하면 그 장르에 집중하는 경향이 있음.
                         예) 카운터 스트라이크 굿즈 조회 -> 닌텐도 굿즈 추천 (금지)
                
                [출력 예시]
                상품명: 디디에 드록바 친필싸인 유니폼. 카테고리: 축구. 설명: 09시즌 첼시 홈 유니폼. 추천 사유: 같은 시즌 인기 선수 관련 굿즈 관심 가능성 판단.
                
                검색 키워드:""".formatted(viewSummary);
        log.info("llm prompt: {}", prompt);
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        log.info("llm response: {}", response);
        return response != null ? response.trim() : "";
    }

    // 인덱싱 결과 record
    private record IndexingResult(int successCount, int failCount) {}
}