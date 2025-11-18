package com.ll.products.domain.product.event;

import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.search.document.ProductDocument;
import com.ll.products.domain.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductSearchRepository productSearchRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductEvent(ProductEvent event) {
        Product product = event.getProduct();
        ProductEvent.EventType eventType = event.getEventType();
        syncToElasticsearchRetry(product, eventType);
    }

    // 재시도 전략
    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void syncToElasticsearchRetry(Product product, ProductEvent.EventType eventType) {
        try {
            if (eventType == ProductEvent.EventType.DELETED) {
                productSearchRepository.deleteById(product.getId());
                log.info("Elasticsearch 삭제 완료 - productId: {}", product.getId());
            } else {
                ProductDocument document = ProductDocument.from(product);
                productSearchRepository.save(document);
                log.info("Elasticsearch 동기화 완료 - eventType: {}, productId: {}", eventType, product.getId());
            }
        } catch (Exception e) {
            log.warn("Elasticsearch 동기화 시도 실패 - eventType: {}, error: {}", eventType, e.getMessage());
            throw e;
        }
    }

    @Recover
    public void recoverFromSyncFailure(Exception e, Product product, ProductEvent.EventType eventType) {
        log.error("동기화 재시도 전략 실패 : {}, {}", eventType, e.getMessage(), e);
    }
}