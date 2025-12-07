package com.ll.products.domain.recommendation.service;

import com.ll.products.domain.recommendation.document.ProductVectorPoint;
import com.ll.products.domain.recommendation.dto.RecommendationResponse;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.*;
import io.qdrant.client.grpc.Points.*;
import io.qdrant.client.grpc.JsonWithInt.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.qdrant.client.ConditionFactory.*;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final QdrantClient qdrantClient;
    private final String qdrantCollectionName;

    private static final int VECTOR_SIZE = 1536;

    // 1. 상품 벡터 저장(단일)
    public void upsertProduct(ProductVectorPoint productVectorPoint) {
        try {
            PointStruct point = buildPointStruct(productVectorPoint);
            qdrantClient.upsertAsync(qdrantCollectionName, List.of(point)).get();
            log.info("상품 벡터 저장 완료: productCode={}", productVectorPoint.getDocument().getProductCode());
        } catch (Exception e) {
            log.error("상품 벡터 저장 실패: productCode={}", productVectorPoint.getDocument().getProductCode(), e);
            throw new RuntimeException("벡터 저장 중 오류 발생", e);
        }
    }

    // 2. 상품 벡터 저장(다중)
    public void upsertProducts(List<ProductVectorPoint> productVectorPoints) {
        if (productVectorPoints == null || productVectorPoints.isEmpty()) {
            log.warn("빈 상품 벡터 리스트로 저장 시도");
            return;
        }
        try {
            List<PointStruct> points = productVectorPoints.stream()
                    .map(this::buildPointStruct)
                    .toList();
            qdrantClient.upsertAsync(qdrantCollectionName, points).get();
            log.info("배치 상품 벡터 저장 완료: count={}", productVectorPoints.size());
        } catch (Exception e) {
            log.error("배치 상품 벡터 저장 실패: count={}", productVectorPoints.size(), e);
            throw new RuntimeException("배치 벡터 저장 중 오류 발생", e);
        }
    }

    // 3. 유사 상품 검색
    public List<RecommendationResponse> searchSimilarProducts(float[] embedding, int limit) {
        try {
            List<ScoredPoint> scoredPoints = qdrantClient.searchAsync(
                    SearchPoints.newBuilder()
                            .setCollectionName(qdrantCollectionName)
                            .addAllVector(convertToList(embedding))
                            .setLimit(limit)
                            .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build())
                            .build()
            ).get();
            List<RecommendationResponse> results = scoredPoints.stream()
                    .map(this::convertToRecommendation)
                    .toList();
            log.info("유사 상품 검색 완료: 결과 개수={}", results.size());
            return results;
        } catch (Exception e) {
            log.error("유사 상품 검색 실패", e);
            throw new RuntimeException("벡터 검색 중 오류 발생", e);
        }
    }

    // 4. 상품 코드로 삭제
    public void deleteProductByCode(String productCode) {
        try {
            Filter filter = Filter.newBuilder()
                    .addMust(matchKeyword("productCode", productCode))
                    .build();

            qdrantClient.deleteAsync(qdrantCollectionName, filter).get();
            log.info("상품 벡터 삭제 완료: productCode={}", productCode);
        } catch (Exception e) {
            log.error("상품 벡터 삭제 실패: productCode={}", productCode, e);
            throw new RuntimeException("벡터 삭제 중 오류 발생", e);
        }
    }

    // 5. 컬렉션 삭제 후 재생성
    public void recreateCollection() {
        deleteCollection();
        createCollection();
    }




    // 컬렉션 생성
    private void createCollection() {
        try {
            log.info("Qdrant 컬렉션 생성 중: {}", qdrantCollectionName);
            qdrantClient.createCollectionAsync(
                    qdrantCollectionName,
                    VectorParams.newBuilder()
                            .setSize(VECTOR_SIZE)
                            .setDistance(Distance.Cosine)
                            .build()
            ).get();
            log.debug("Qdrant 컬렉션 생성 완료: {}", qdrantCollectionName);
        } catch (Exception e) {
            log.warn("Qdrant 컬렉션 생성 실패. 추천 기능은 사용할 수 없습니다.", e);
        }
    }

    // 컬렉션 삭제
    private void deleteCollection() {
        try {
            log.info("Qdrant 컬렉션 삭제 중: {}", qdrantCollectionName);
            qdrantClient.deleteCollectionAsync(qdrantCollectionName).get();
            log.info("Qdrant 컬렉션 삭제 완료: {}", qdrantCollectionName);
        } catch (Exception e) {
            log.warn("Qdrant 컬렉션 삭제 실패 (컬렉션이 없을 수 있음): {}", e.getMessage());
        }
    }

    // pointStruct 생성
    private PointStruct buildPointStruct(ProductVectorPoint vectorPoint) {
        return PointStruct.newBuilder()
                .setId(id(UUID.fromString(vectorPoint.getDocument().getProductCode())))
                .setVectors(vectors(vectorPoint.getEmbedding()))
                .putAllPayload(convertToValueMap(vectorPoint.getDocument().toPayload()))
                .build();
    }

    // score -> response 변환
    private RecommendationResponse convertToRecommendation(ScoredPoint scoredPoint) {
        Map<String, Value> payload = scoredPoint.getPayloadMap();
        return RecommendationResponse.builder()
                .productCode(payload.get("productCode").getStringValue())
                .name(payload.get("name").getStringValue())
                .description(payload.get("description").getStringValue())
                .categoryName(payload.get("categoryName").getStringValue())
                .price((int) payload.get("price").getIntegerValue())
                .status(payload.get("status").getStringValue())
                .score(scoredPoint.getScore())
                .build();
    }

    // object -> value 변환
    private Map<String, Value> convertToValueMap(Map<String, Object> payload) {
        Map<String, Value> valueMap = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof Integer) {
                valueMap.put(entry.getKey(), value((Integer) obj));
            } else {
                valueMap.put(entry.getKey(), value(obj != null ? obj.toString() : ""));
            }
        }
        return valueMap;
    }

    // float -> list 변환
    private List<Float> convertToList(float[] array) {
        List<Float> list = new java.util.ArrayList<>();
        for (float val : array) {
            list.add(val);
        }
        return list;
    }
}