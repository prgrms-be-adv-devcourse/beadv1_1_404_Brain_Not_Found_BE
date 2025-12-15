package com.ll.products.domain.recommendation.service;

import com.ll.core.model.vo.kafka.ProductEvent;
import com.ll.core.model.vo.kafka.enums.ProductEventType;
import com.ll.products.domain.recommendation.document.ProductVectorDocument;
import com.ll.products.domain.recommendation.document.ProductVectorPoint;
import com.ll.products.domain.recommendation.exception.VectorIndexException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VectorIndexService 테스트")
class VectorIndexServiceTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private VectorStoreService vectorStoreService;

    @InjectMocks
    private VectorIndexService vectorIndexService;

    private ProductEvent testEvent;
    private float[] testEmbedding;

    @BeforeEach
    void setUp() {
        testEvent = ProductEvent.builder()
                .eventType(ProductEventType.PRODUCT_UPDATED_STATUS)
                .productCode("PROD-001")
                .name("테스트 상품")
                .description("테스트 상품 설명입니다")
                .categoryName("전자제품")
                .price(50000)
                .quantity(10)
                .status("ON_SALE")
                .categoryId(1L)
                .mainImageFileKey("image-key-001")
                .createdAt(LocalDateTime.now().toString())
                .build();
        testEmbedding = new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
    }

    @Test
    @DisplayName("1. 상품 인덱싱 성공")
    void indexProductSuccess() {
        // given
        when(embeddingService.generateEmbedding(anyString()))
                .thenReturn(testEmbedding);
        doNothing().when(vectorStoreService).upsertProduct(any(ProductVectorPoint.class));

        // when
        vectorIndexService.indexProduct(testEvent);

        // then
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductVectorPoint> pointCaptor = ArgumentCaptor.forClass(ProductVectorPoint.class);
        verify(embeddingService).generateEmbedding(textCaptor.capture());
        verify(vectorStoreService).upsertProduct(pointCaptor.capture());
        String embeddingText = textCaptor.getValue();
        assertThat(embeddingText).contains("테스트 상품");
        assertThat(embeddingText).contains("테스트 상품 설명입니다");
        assertThat(embeddingText).contains("전자제품");
        ProductVectorPoint point = pointCaptor.getValue();
        assertThat(point.getDocument().getProductCode()).isEqualTo("PROD-001");
        assertThat(point.getDocument().getName()).isEqualTo("테스트 상품");
        assertThat(point.getEmbedding()).isEqualTo(testEmbedding);
    }

    @Test
    @DisplayName("1-1. 상품 인덱싱 실패 (임베딩 생성 실패)")
    void indexProductEmbeddingFailed() {
        // given
        when(embeddingService.generateEmbedding(anyString()))
                .thenThrow(new RuntimeException());

        // when
        // then
        assertThatThrownBy(() -> vectorIndexService.indexProduct(testEvent))
                .isInstanceOf(VectorIndexException.class)
                .hasMessage("벡터 인덱싱에 실패했습니다.");
        verify(embeddingService).generateEmbedding(anyString());
        verify(vectorStoreService, never()).upsertProduct(any());
    }

    @Test
    @DisplayName("1-2. 상품 인덱싱 실패 (벡터 저장 실패)")
    void indexProductVectorStoreFailed() {
        // given
        when(embeddingService.generateEmbedding(anyString())).thenReturn(testEmbedding);
        doThrow(new RuntimeException("벡터 저장 실패"))
                .when(vectorStoreService).upsertProduct(any(ProductVectorPoint.class));

        // when
        // then
        assertThatThrownBy(() -> vectorIndexService.indexProduct(testEvent))
                .isInstanceOf(VectorIndexException.class)
                .hasMessage("벡터 인덱싱에 실패했습니다.");
        verify(embeddingService).generateEmbedding(anyString());
        verify(vectorStoreService).upsertProduct(any());
    }

    @Test
    @DisplayName("2. 벡터 인덱스 삭제 성공")
    void deleteProductIndexSuccess() {
        // given
        String productCode = "PROD-001";
        doNothing().when(vectorStoreService).deleteProductByCode(productCode);

        // when
        vectorIndexService.deleteProductIndex(productCode);

        // then
        verify(vectorStoreService).deleteProductByCode(productCode);
    }

    @Test
    @DisplayName("2-1. 벡터 인덱스 삭제 실패")
    void deleteProductIndexFailed() {
        // given
        String productCode = "PROD-001";
        doThrow(new RuntimeException("삭제 실패"))
                .when(vectorStoreService).deleteProductByCode(productCode);

        // when
        // then
        assertThatThrownBy(() -> vectorIndexService.deleteProductIndex(productCode))
                .isInstanceOf(VectorIndexException.class)
                .hasMessage("벡터 인덱스 삭제에 실패했습니다.");
        verify(vectorStoreService).deleteProductByCode(productCode);
    }
}