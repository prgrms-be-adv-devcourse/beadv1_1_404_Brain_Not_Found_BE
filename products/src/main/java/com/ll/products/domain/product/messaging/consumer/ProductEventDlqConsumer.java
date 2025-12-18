package com.ll.products.domain.product.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.products.domain.product.model.entity.DlqStatus;
import com.ll.products.domain.product.model.entity.ProductDlqEvent;
import com.ll.products.domain.product.repository.ProductDlqEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventDlqConsumer {

    private final ProductDlqEventRepository productDlqEventRepository;
    private final ObjectMapper objectMapper;


    private static final String PRODUCT_DLQ_TOPIC = "product-event.dlq";
    private static final String DLQ_GROUP_ID = "product-dlq-group";

    @KafkaListener(
            topics = PRODUCT_DLQ_TOPIC,
            groupId = DLQ_GROUP_ID
    )
    public void consumeDlqEvent(ConsumerRecord<String, Object> record) {
        log.error("DLQ 이벤트 수신: topic={}, partition={}, offset={}, key={}",
                record.topic(), record.partition(), record.offset(), record.key());
        try {
            ProductDlqEvent dlqEvent = buildDlqEvent(record);
            productDlqEventRepository.save(dlqEvent);
            log.info("DLQ 이벤트 저장 완료: id={}, topic={}",
                    dlqEvent.getId(), record.topic());
        } catch (Exception e) {
            log.error("DLQ 이벤트 저장 실패: topic={}, error={}", record.topic(), e.getMessage(), e);
        }
    }

    // record -> productDlqEvent
    private ProductDlqEvent buildDlqEvent(ConsumerRecord<String, Object> record) throws JsonProcessingException {
        String eventPayload = objectMapper.writeValueAsString(record.value());
        ProductDlqEvent dlqEvent = ProductDlqEvent.builder()
                .topic(record.topic())
                .eventPayload(eventPayload)
                .failedAt(LocalDateTime.now())
                .status(DlqStatus.PENDING)
                .build();
        return dlqEvent;
    }
}