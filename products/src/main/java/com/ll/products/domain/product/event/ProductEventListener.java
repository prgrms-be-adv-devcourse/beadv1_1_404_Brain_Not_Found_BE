package com.ll.products.domain.product.event;

import com.ll.products.domain.product.messaging.producer.ProductEventProducer;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.search.document.ProductDocument;
import com.ll.products.domain.search.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductEventProducer productEventProducer;
    private final ProductSearchRepository productSearchRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductEvent(ProductsEvent event) {
        Product product = event.getProduct();
        ProductsEvent.EventType eventType = event.getEventType();
        try {
            switch (eventType) {
                case UPDATED -> {
                    log.debug("상품 수정 이벤트 처리: productCode={}", product.getCode());
                    ProductDocument document = ProductDocument.from(product);
                    productSearchRepository.save(document);
                    log.debug("Elasticsearch 상품 수정 동기화 완료 - eventType: {}, productId: {}", eventType, product.getId());
                    productEventProducer.publishProductUpdated(product);
                }
                case DELETED -> {
                    log.debug("상품 삭제 이벤트 처리: productCode={}", product.getCode());
                    productSearchRepository.deleteById(product.getId());
                    log.debug("Elasticsearch 삭제 완료 - productId: {}", product.getId());
                    productEventProducer.publishProductDeleted(product);
                }
                case UPDATED_STATUS -> {
                    log.debug("상품 상태 변경 이벤트 처리: productCode={}", product.getCode());
                    ProductDocument document = ProductDocument.from(product);
                    productSearchRepository.save(document);
                    log.debug("Elasticsearch 상품 상태 변경 동기화 완료 - eventType: {}, productId: {}", eventType, product.getId());
                    productEventProducer.publishProductUpdatedStatus(product);
                }
            }
        } catch (Exception e) {
            log.error("Kafka 이벤트 발행 실패: productCode={}, eventType={}, error={}", product.getCode(), eventType, e.getMessage(), e);
        }
    }
}