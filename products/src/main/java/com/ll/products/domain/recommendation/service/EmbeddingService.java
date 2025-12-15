package com.ll.products.domain.recommendation.service;

import com.ll.products.domain.recommendation.exception.EmbeddingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    // 1. 임베딩(단일)
    public float[] generateEmbedding(String text) {
        String validatedText = validateText(text);
        try {
            log.debug("임베딩 생성 중");
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(validatedText));
            float[] embedding = getEmbeddingFromResponse(response);
            log.debug("임베딩 생성 완료");
            return embedding;
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            log.error("임베딩 생성 실패", e);
            throw new EmbeddingException("임베딩 생성 중 오류가 발생했습니다.");
        }
    }

    // 2. 임베딩(다중)
    public List<float[]> generateEmbeddings(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            log.warn("빈 텍스트 리스트로 임베딩 생성");
            return List.of();
        }
        List<String> validatedTexts = texts.stream()
                .map(this::validateText)
                .toList();
        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(validatedTexts);
            List<float[]> embeddings = getEmbeddingsFromResponse(response, validatedTexts.size());
            log.info("배치 임베딩 생성 완료: count={}", embeddings.size());
            return embeddings;
        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            log.error("배치 임베딩 생성 실패", e);
            throw new EmbeddingException("배치 임베딩 생성 중 오류가 발생했습니다.");
        }
    }

    // 텍스트 검증
    private String validateText(String text) {
        if (text == null || text.isBlank()) {
            log.warn("빈 텍스트로 임베딩 생성");
            return "empty";
        }
        return text;
    }

    // 임베딩 검증 및 반환(단일)
    private float[] getEmbeddingFromResponse(EmbeddingResponse response) {
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            throw new EmbeddingException("임베딩 응답이 비어있습니다.");
        }

        float[] embedding = response.getResults().getFirst().getOutput();
        if (embedding == null || embedding.length == 0) {
            throw new EmbeddingException("임베딩 벡터가 비어있습니다.");
        }
        return embedding;
    }


    // 임베딩 검증 및 반환(다중)
    private List<float[]> getEmbeddingsFromResponse(EmbeddingResponse response, int expectedCount) {
        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            throw new EmbeddingException("임베딩 응답이 비어있습니다.");
        }
        List<float[]> embeddings = response.getResults().stream()
                .map(result -> result.getOutput())
                .toList();

        if (embeddings.size() != expectedCount) {
            throw new EmbeddingException("임베딩 개수가 일치하지 않습니다.");
        }
        return embeddings;
    }
}