package com.ll.products.domain.recommendation.service;

import com.ll.products.domain.recommendation.exception.EmbeddingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.Embedding;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmbeddingService 테스트")
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @InjectMocks
    private EmbeddingService embeddingService;

    private float[] testEmbedding;
    private EmbeddingResponse testResponse;

    @BeforeEach
    void setUp() {
        testEmbedding = new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
        Embedding embedding = new Embedding(testEmbedding, 0);
        testResponse = new EmbeddingResponse(List.of(embedding));
    }

    @Test
    @DisplayName("1. 단일 텍스트 임베딩 생성 성공")
    void generateEmbeddingSuccess() {
        // given
        String text = "테스트 상품입니다";
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(testResponse);

        // when
        float[] result = embeddingService.generateEmbedding(text);

        // then
        assertThat(result).isEqualTo(testEmbedding);
        verify(embeddingModel).embedForResponse(anyList());
    }

    @Test
    @DisplayName("1-1. 단일 텍스트 임베딩 (빈 텍스트)")
    void generateEmbeddingWithEmptyText() {
        // given
        String text = "";
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(testResponse);

        // when
        float[] result = embeddingService.generateEmbedding(text);

        // then
        assertThat(result).isNotNull();
        verify(embeddingModel).embedForResponse(List.of("empty"));
    }

    @Test
    @DisplayName("1-2. 단일 텍스트 임베딩 (텍스트 null)")
    void generateEmbeddingWithNullText() {
        // given
        String text = null;
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(testResponse);

        // when
        float[] result = embeddingService.generateEmbedding(text);

        // then
        assertThat(result).isNotNull();
        verify(embeddingModel).embedForResponse(List.of("empty"));
    }

    @Test
    @DisplayName("1-3. 단일 텍스트 임베딩 (임베딩 생성 중 오류)")
    void generateEmbeddingThrowsException() {
        // given
        String text = "테스트 상품입니다";
        when(embeddingModel.embedForResponse(anyList()))
                .thenThrow(new RuntimeException());

        // when
        // then
        assertThatThrownBy(() -> embeddingService.generateEmbedding(text))
                .isInstanceOf(EmbeddingException.class)
                .hasMessage("임베딩 생성 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("1-4. 단일 텍스트 임베딩 (응답이 null)")
    void generateEmbeddingWithNullResponse() {
        // given
        String text = "테스트 상품입니다";
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> embeddingService.generateEmbedding(text))
                .isInstanceOf(EmbeddingException.class)
                .hasMessage("임베딩 응답이 비어있습니다.");
    }

    @Test
    @DisplayName("1-5. 단일 텍스트 임베딩 (빈 응답)")
    void generateEmbeddingWithEmptyResults() {
        // given
        String text = "테스트 상품입니다";
        EmbeddingResponse emptyResponse = new EmbeddingResponse(List.of());
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(emptyResponse);

        // when
        // then
        assertThatThrownBy(() -> embeddingService.generateEmbedding(text))
                .isInstanceOf(EmbeddingException.class)
                .hasMessage("임베딩 응답이 비어있습니다.");
    }

    @Test
    @DisplayName("1-5. 단일 텍스트 임베딩 (빈 임베딩 벡터)")
    void generateEmbeddingWithEmptyVector() {
        // given
        String text = "테스트 상품입니다";
        Embedding emptyEmbedding = new Embedding(new float[]{}, 0);
        EmbeddingResponse response = new EmbeddingResponse(List.of(emptyEmbedding));
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(response);

        // when
        // then
        assertThatThrownBy(() -> embeddingService.generateEmbedding(text))
                .isInstanceOf(EmbeddingException.class)
                .hasMessage("임베딩 벡터가 비어있습니다.");
    }

    @Test
    @DisplayName("2. 배치 임베딩 생성 성공")
    void generateEmbeddingsSuccess() {
        // given
        List<String> texts = List.of("상품1", "상품2", "상품3");
        float[] embedding1 = new float[]{0.1f, 0.2f};
        float[] embedding2 = new float[]{0.3f, 0.4f};
        float[] embedding3 = new float[]{0.5f, 0.6f};
        List<Embedding> embeddings = List.of(
                new Embedding(embedding1, 0),
                new Embedding(embedding2, 1),
                new Embedding(embedding3, 2)
        );
        EmbeddingResponse response = new EmbeddingResponse(embeddings);
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(response);

        // when
        List<float[]> result = embeddingService.generateEmbeddings(texts);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo(embedding1);
        assertThat(result.get(1)).isEqualTo(embedding2);
        assertThat(result.get(2)).isEqualTo(embedding3);
        verify(embeddingModel).embedForResponse(anyList());
    }

    @Test
    @DisplayName("2-1. 배치 임베딩 생성 (빈 리스트)")
    void generateEmbeddingsWithEmptyList() {
        // given
        List<String> texts = List.of();

        // when
        List<float[]> result = embeddingService.generateEmbeddings(texts);

        // then
        assertThat(result).isEmpty();
        verify(embeddingModel, never()).embedForResponse(anyList());
    }

    @Test
    @DisplayName("2-2. 배치 임베딩 생성 (리스트 null)")
    void generateEmbeddingsWithNullList() {
        // given
        List<String> texts = null;

        // when
        List<float[]> result = embeddingService.generateEmbeddings(texts);

        // then
        assertThat(result).isEmpty();
        verify(embeddingModel, never()).embedForResponse(anyList());
    }

    @Test
    @DisplayName("2-3. 배치 임베딩 생성 (임베딩 생성 중 오류)")
    void generateEmbeddingsThrowsException() {
        // given
        List<String> texts = List.of("상품1", "상품2");
        when(embeddingModel.embedForResponse(anyList()))
                .thenThrow(new RuntimeException());

        // when
        // then
        assertThatThrownBy(() -> embeddingService.generateEmbeddings(texts))
                .isInstanceOf(EmbeddingException.class)
                .hasMessage("배치 임베딩 생성 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("2-4. 배치 임베딩 생성 (임베딩 수량 오류)")
    void generateEmbeddingsWithMismatchedCount() {
        // given
        List<String> texts = List.of("상품1", "상품2", "상품3");
        float[] embedding1 = new float[]{0.1f, 0.2f};
        float[] embedding2 = new float[]{0.3f, 0.4f};
        List<Embedding> embeddings = List.of(
                new Embedding(embedding1, 0),
                new Embedding(embedding2, 1)
        );
        EmbeddingResponse response = new EmbeddingResponse(embeddings);
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(response);

        // when & then
        assertThatThrownBy(() -> embeddingService.generateEmbeddings(texts))
                .isInstanceOf(EmbeddingException.class)
                .hasMessage("임베딩 개수가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("2-5. 배치 임베딩 생성 (빈 텍스트)")
    void generateEmbeddingsWithEmptyTexts() {
        // given
        List<String> texts = java.util.Arrays.asList("상품1", "", null);
        float[] embedding1 = new float[]{0.1f, 0.2f};
        float[] embedding2 = new float[]{0.3f, 0.4f};
        float[] embedding3 = new float[]{0.5f, 0.6f};
        List<Embedding> embeddings = List.of(
                new Embedding(embedding1, 0),
                new Embedding(embedding2, 1),
                new Embedding(embedding3, 2)
        );
        EmbeddingResponse response = new EmbeddingResponse(embeddings);
        when(embeddingModel.embedForResponse(anyList()))
                .thenReturn(response);

        // when
        List<float[]> result = embeddingService.generateEmbeddings(texts);

        // then
        assertThat(result).hasSize(3);
        verify(embeddingModel).embedForResponse(List.of("상품1", "empty", "empty"));
    }
}