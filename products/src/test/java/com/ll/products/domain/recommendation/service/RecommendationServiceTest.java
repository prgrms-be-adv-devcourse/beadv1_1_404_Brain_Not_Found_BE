package com.ll.products.domain.recommendation.service;

import com.ll.products.domain.cart.model.enums.Role;
import com.ll.products.domain.history.service.HistoryFacadeService;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.ProductRepository;
import com.ll.products.domain.recommendation.document.ProductVectorDocument;
import com.ll.products.domain.recommendation.document.ProductVectorPoint;
import com.ll.products.domain.recommendation.dto.RecommendationResponse;
import com.ll.products.domain.recommendation.exception.VectorNotFoundException;
import io.qdrant.client.grpc.JsonWithInt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService 테스트")
class RecommendationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private VectorStoreService vectorStoreService;

    @Mock
    private HistoryFacadeService historyFacadeService;

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private RecommendationService recommendationService;

    private float[] testEmbedding;
    private List<RecommendationResponse> testRecommendations;
    private RecommendationResponse testRecommendation1;
    private RecommendationResponse testRecommendation2;
    private RecommendationResponse testRecommendation3;

    @BeforeEach
    void setUp() {
        testEmbedding = new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
        testRecommendation1 = RecommendationResponse.builder()
                .productCode("PROD-001")
                .name("추천 상품 1")
                .description("추천 상품 1 설명")
                .categoryName("전자제품")
                .price(50000)
                .status("ON_SALE")
                .score(0.95f)
                .build();
        testRecommendation2 = RecommendationResponse.builder()
                .productCode("PROD-002")
                .name("추천 상품 2")
                .description("추천 상품 2 설명")
                .categoryName("의류")
                .price(30000)
                .status("ON_SALE")
                .score(0.90f)
                .build();
        testRecommendation3 = RecommendationResponse.builder()
                .productCode("PROD-003")
                .name("추천 상품 3")
                .description("추천 상품 3 설명")
                .categoryName("도서")
                .price(15000)
                .status("ON_SALE")
                .score(0.85f)
                .build();
        testRecommendations = List.of(testRecommendation1, testRecommendation2, testRecommendation3);
    }

    @Test
    @DisplayName("1. 유사 상품 추천 성공 (자기 자신 제외)")
    void recommendSimilarProductsSuccess() {
        // given
        String productCode = "PROD-001";
        int limit = 2;
        when(vectorStoreService.getVectorByProductCode(productCode))
                .thenReturn(testEmbedding);
        when(vectorStoreService.searchSimilarProducts(testEmbedding, limit + 1))
                .thenReturn(testRecommendations);

        // when
        List<RecommendationResponse> results = recommendationService.recommendSimilarProducts(productCode, limit);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).doesNotContain(testRecommendation1);
        assertThat(results).contains(testRecommendation2, testRecommendation3);
        verify(vectorStoreService).getVectorByProductCode(productCode);
        verify(vectorStoreService).searchSimilarProducts(testEmbedding, limit + 1);
    }

    @Test
    @DisplayName("2. 키워드 기반 상품 추천 성공")
    void recommendProductsByKeywordSuccess() {
        // given
        String keyword = "노트북";
        int limit = 3;
        when(embeddingService.generateEmbedding(keyword))
                .thenReturn(testEmbedding);
        when(vectorStoreService.searchSimilarProducts(testEmbedding, limit))
                .thenReturn(testRecommendations);

        // when
        List<RecommendationResponse> results = recommendationService.recommendProductsByKeyword(keyword, limit);

        // then
        assertThat(results).hasSize(3);
        assertThat(results).isEqualTo(testRecommendations);
        verify(embeddingService).generateEmbedding(keyword);
        verify(vectorStoreService).searchSimilarProducts(testEmbedding, limit);
    }

    @Test
    @DisplayName("3. 조회 기록 기반 상품 추천 성공")
    void recommendProductsByViewHistorySuccess() {
        // given
        String userCode = "USER-001";
        int limit = 2;
        List<String> viewList = List.of("PROD-001", "PROD-002");
        float[] embedding1 = new float[]{0.1f, 0.2f, 0.3f};
        float[] embedding2 = new float[]{0.2f, 0.3f, 0.4f};
        when(historyFacadeService.getViewList(userCode))
                .thenReturn(viewList);
        when(vectorStoreService.getVectorByProductCode("PROD-001"))
                .thenReturn(embedding1);
        when(vectorStoreService.getVectorByProductCode("PROD-002"))
                .thenReturn(embedding2);
        when(vectorStoreService.searchSimilarProducts(any(float[].class), eq(limit + viewList.size())))
                .thenReturn(testRecommendations);

        // when
        List<RecommendationResponse> results = recommendationService.recommendProductsByViewHistory(userCode, limit);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).productCode()).isEqualTo("PROD-003");
        verify(historyFacadeService).getViewList(userCode);
        verify(vectorStoreService, times(2)).getVectorByProductCode(anyString());
        verify(vectorStoreService).searchSimilarProducts(any(float[].class), eq(limit + viewList.size()));
    }

    @Test
    @DisplayName("3-1. 조회 기록 기반 상품 추천 (기록 없음)")
    void recommendProductsByViewHistoryNoHistory() {
        // given
        String userCode = "USER-001";
        int limit = 3;
        when(historyFacadeService.getViewList(userCode))
                .thenReturn(List.of());

        // when
        List<RecommendationResponse> results = recommendationService.recommendProductsByViewHistory(userCode, limit);

        // then
        assertThat(results).isEmpty();
        verify(historyFacadeService).getViewList(userCode);
        verify(vectorStoreService, never()).getVectorByProductCode(anyString());
        verify(vectorStoreService, never()).searchSimilarProducts(any(float[].class), anyInt());
    }

    @Test
    @DisplayName("3-2. 조회 기록 기반 상품 추천 (기록 null)")
    void recommendProductsByViewHistoryNullHistory() {
        // given
        String userCode = "USER-001";
        int limit = 3;
        when(historyFacadeService.getViewList(userCode))
                .thenReturn(null);

        // when
        List<RecommendationResponse> results = recommendationService.recommendProductsByViewHistory(userCode, limit);

        // then
        assertThat(results).isEmpty();
        verify(historyFacadeService).getViewList(userCode);
    }

    @Test
    @DisplayName("3-3. 조회 기록 기반 상품 추천 (일부 벡터 조회 실패)")
    void recommendProductsByViewHistoryPartialVectorFailed() {
        // given
        String userCode = "USER-001";
        int limit = 2;
        List<String> viewList = List.of("PROD-004", "PROD-005", "PROD-006");
        float[] embedding4 = new float[]{0.1f, 0.2f, 0.3f};
        float[] embedding6 = new float[]{0.3f, 0.4f, 0.5f};

        when(historyFacadeService.getViewList(userCode))
                .thenReturn(viewList);
        when(vectorStoreService.getVectorByProductCode("PROD-004"))
                .thenReturn(embedding4);
        when(vectorStoreService.getVectorByProductCode("PROD-005"))
                .thenThrow(new VectorNotFoundException("PROD-005"));
        when(vectorStoreService.getVectorByProductCode("PROD-006"))
                .thenReturn(embedding6);
        when(vectorStoreService.searchSimilarProducts(any(float[].class), anyInt()))
                .thenReturn(testRecommendations);

        // when
        List<RecommendationResponse> results = recommendationService.recommendProductsByViewHistory(userCode, limit);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).contains(testRecommendation1, testRecommendation2);
        verify(vectorStoreService, times(3)).getVectorByProductCode(anyString());
        verify(vectorStoreService).searchSimilarProducts(any(float[].class), anyInt());
    }

    @Test
    @DisplayName("3-4. 조회 기록 기반 상품 추천 (모든 벡터 조회 실패)")
    void recommendProductsByViewHistoryAllVectorsFailed() {
        // given
        String userCode = "USER-001";
        int limit = 2;
        List<String> viewList = List.of("PROD-001", "PROD-002");
        when(historyFacadeService.getViewList(userCode))
                .thenReturn(viewList);
        when(vectorStoreService.getVectorByProductCode(anyString()))
                .thenThrow(new VectorNotFoundException("Not found"));

        // when
        // then
        assertThatThrownBy(() -> recommendationService.recommendProductsByViewHistory(userCode, limit))
                .isInstanceOf(VectorNotFoundException.class);
        verify(vectorStoreService, times(2)).getVectorByProductCode(anyString());
        verify(vectorStoreService, never()).searchSimilarProducts(any(float[].class), anyInt());
    }

    @Test
    @DisplayName("4. 검색 기록 기반 상품 추천 성공")
    void recommendProductsBySearchHistorySuccess() {
        // given
        String userCode = "USER-001";
        int limit = 3;
        List<String> searchList = List.of("노트북", "마우스", "키보드");
        when(historyFacadeService.getSearchList(userCode))
                .thenReturn(searchList);
        when(embeddingService.generateEmbedding("노트북 마우스 키보드"))
                .thenReturn(testEmbedding);
        when(vectorStoreService.searchSimilarProducts(testEmbedding, limit))
                .thenReturn(testRecommendations);

        // when
        List<RecommendationResponse> results = recommendationService.recommendProductsBySearchHistory(userCode, limit);

        // then
        assertThat(results).hasSize(3);
        assertThat(results).isEqualTo(testRecommendations);
        verify(historyFacadeService).getSearchList(userCode);
        verify(embeddingService).generateEmbedding("노트북 마우스 키보드");
        verify(vectorStoreService).searchSimilarProducts(testEmbedding, limit);
    }

    @Test
    @DisplayName("4-1. 검색 기록 기반 상품 추천 (기록 없음)")
    void recommendProductsBySearchHistoryNoHistory() {
        // given
        String userCode = "USER-001";
        int limit = 3;
        when(historyFacadeService.getSearchList(userCode))
                .thenReturn(List.of());

        // when
        List<RecommendationResponse> results = recommendationService.recommendProductsBySearchHistory(userCode, limit);

        // then
        assertThat(results).isEmpty();
        verify(historyFacadeService).getSearchList(userCode);
        verify(embeddingService, never()).generateEmbedding(anyString());
    }

    @Test
    @DisplayName("4-2. 검색 기록 기반 상품 추천 (기록 null)")
    void recommendProductsBySearchHistoryNullHistory() {
        // given
        String userCode = "USER-001";
        int limit = 3;
        when(historyFacadeService.getSearchList(userCode))
                .thenReturn(null);

        // when
        List<RecommendationResponse> results = recommendationService.recommendProductsBySearchHistory(userCode, limit);

        // then
        assertThat(results).isEmpty();
        verify(historyFacadeService, times(1)).getSearchList(userCode);
    }

    @Test
    @DisplayName("5. 전체 상품 재인덱싱 성공")
    void reindexAllProductsSuccess() {
        // given
        Product product1 = Product.builder()
                .name("상품 1")
                .description("상품 1 설명")
                .price(10000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        Product product2 = Product.builder()
                .name("상품 2")
                .description("상품 2 설명")
                .price(20000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        List<Product> products = List.of(product1, product2);
        when(productRepository.findAllByIsDeletedFalseAndStatus(ProductStatus.ON_SALE))
                .thenReturn(products);
        when(embeddingService.generateEmbeddings(anyList()))
                .thenReturn(List.of(testEmbedding, testEmbedding));
        doNothing().when(vectorStoreService).recreateCollection();
        doNothing().when(vectorStoreService).upsertProducts(anyList());

        // when
        recommendationService.reindexAllProducts(Role.ADMIN.name());

        // then
        verify(vectorStoreService).recreateCollection();
        verify(productRepository).findAllByIsDeletedFalseAndStatus(ProductStatus.ON_SALE);
        verify(embeddingService).generateEmbeddings(anyList());
        verify(vectorStoreService).upsertProducts(anyList());
    }
}