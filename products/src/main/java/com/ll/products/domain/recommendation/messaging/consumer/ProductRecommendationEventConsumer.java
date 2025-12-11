package com.ll.products.domain.recommendation.messaging.consumer;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.model.vo.kafka.ProductEvent;
import com.ll.core.model.vo.kafka.enums.ProductEventType;
import com.ll.products.domain.recommendation.service.VectorIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRecommendationEventConsumer {

    private final VectorIndexService vectorIndexService;

    private static final String PRODUCT_UPDATE_TOPIC = "product-updated-event";
    private static final String PRODUCT_DELETE_TOPIC = "product-deleted-event";
    private static final String PRODUCT_UPDATE_STATUS_TOPIC = "product-updated-status-event";
    private static final String PRODUCT_GROUP_ID = "product-indexing-group";

    @KafkaListener(
            topics = PRODUCT_UPDATE_TOPIC,
            groupId = PRODUCT_GROUP_ID
    )
    public void consumeProductUpdatedEvent(KafkaEventEnvelope<ProductEvent> envelope) {
        try {
            ProductEvent event = envelope.payload();
            log.info("상품 수정 이벤트 수신: eventType={}, productCode={}", event.eventType(), event.productCode());
            processEvent(event);
            log.info("벡터 수정 인덱싱 완료: productCode={}", event.productCode());
        } catch (Exception e) {
            log.error("벡터 인덱싱 실패: error={}", e.getMessage(), e);
            throw new RuntimeException("벡터 수정 인덱싱 중 오류 발생", e);
        }
    }

    @KafkaListener(
            topics = PRODUCT_DELETE_TOPIC,
            groupId = PRODUCT_GROUP_ID
    )
    public void consumeProductDeletedEvent(KafkaEventEnvelope<ProductEvent> envelope) {
        try {
            ProductEvent event = envelope.payload();
            log.info("상품 삭제 이벤트 수신: eventType={}, productCode={}", event.eventType(), event.productCode());
            processEvent(event);
            log.info("벡터 삭제 인덱싱 완료: productCode={}", event.productCode());
        } catch (Exception e) {
            log.error("벡터 인덱싱 실패: error={}", e.getMessage(), e);
            throw new RuntimeException("벡터 삭제 인덱싱 중 오류 발생", e);
        }
    }

    @KafkaListener(
            topics = PRODUCT_UPDATE_STATUS_TOPIC,
            groupId = PRODUCT_GROUP_ID
    )
    public void consumeProductUpdatedStatusEvent(KafkaEventEnvelope<ProductEvent> envelope) {
        try {
            ProductEvent event = envelope.payload();
            log.info("상품 상태 변경 이벤트 수신: eventType={}, productCode={}", event.eventType(), event.productCode());
            processEvent(event);
            log.info("벡터 상태 변경 인덱싱 완료: productCode={}", event.productCode());
        } catch (Exception e) {
            log.error("벡터 인덱싱 실패: error={}", e.getMessage(), e);
            throw new RuntimeException("벡터 상태 변경 인덱싱 중 오류 발생", e);
        }
    }

    private void processEvent(ProductEvent event) {
        ProductEventType eventType = event.eventType();
        switch (eventType) {
            case PRODUCT_UPDATED, PRODUCT_UPDATED_STATUS -> {
                log.debug("벡터 DB 인덱싱 시작: productCode={}", event.productCode());
                vectorIndexService.indexProduct(event);
            }
            case PRODUCT_DELETED -> {
                log.debug("벡터 DB 삭제 시작: productCode={}", event.productCode());
                vectorIndexService.deleteProductIndex(event.productCode());
            }
            default -> log.warn("알 수 없는 이벤트 타입: {}", eventType);
        }
    }
}