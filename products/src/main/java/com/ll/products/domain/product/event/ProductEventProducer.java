package com.ll.products.domain.product.event;

import com.ll.core.config.kafka.KafkaEventPublisher;
import com.ll.core.model.vo.kafka.ProductEvent;
import com.ll.core.model.vo.kafka.enums.ProductEventType;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private static final String PRODUCT_UPDATE_TOPIC = "product-updated-event";
    private static final String PRODUCT_DELETE_TOPIC = "product-deleted-event";
    private static final String PRODUCT_UPDATE_STATUS_TOPIC = "product-updated-status-event";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final KafkaEventPublisher kafkaEventPublisher;

    // 1. 상품 수정 이벤트 발행
    public void publishProductUpdated(Product product) {
        ProductEvent event = buildProductEvent(product, ProductEventType.PRODUCT_UPDATED);
        kafkaEventPublisher.publish(PRODUCT_UPDATE_TOPIC, event);
        log.info("상품 수정 이벤트 발행: productCode={}", product.getCode());
    }

    // 2. 상품 삭제 이벤트 발행
    public void publishProductDeleted(Product product) {
        ProductEvent event = buildProductEvent(product, ProductEventType.PRODUCT_DELETED);
        kafkaEventPublisher.publish(PRODUCT_DELETE_TOPIC, event);
        log.info("상품 삭제 이벤트 발행: productCode={}", product.getCode());
    }

    // 3. 상품 상태 변경 이벤트 발행
    public void publishProductUpdatedStatus(Product product) {
        ProductEvent event = buildProductEvent(product, ProductEventType.PRODUCT_UPDATED_STATUS);
        kafkaEventPublisher.publish(PRODUCT_UPDATE_STATUS_TOPIC, event);
        log.info("상품 상태 수정 이벤트 발행: productCode={}, status={}", product.getCode(), product.getStatus().name());
    }


    // Product -> event 변환
    private ProductEvent buildProductEvent(Product product, ProductEventType eventType) {
        String mainImageFileKey = product.getImages().stream()
                .filter(ProductImage::getIsMain)
                .findFirst()
                .map(ProductImage::getFileKey)
                .orElse(null);
        return ProductEvent.builder()
                .eventType(eventType)
                .productCode(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .status(product.getStatus().name())
                .mainImageFileKey(mainImageFileKey)
                .createdAt(product.getCreatedAt().format(FORMATTER))
                .updatedAt(product.getUpdatedAt().format(FORMATTER))
                .build();
    }
}
