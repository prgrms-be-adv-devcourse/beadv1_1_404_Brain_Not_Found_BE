package com.ll.core.config.kafka;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import com.ll.core.tracing.CorrelationContext;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.application.name}")
    private String moduleName;

    public <T> void publish(String topic, T payload) {
        KafkaEventEnvelope<T> envelope = KafkaEventEnvelope.wrap(moduleName, CorrelationContext.get(), payload);

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, envelope);

        // 분석용 중복 헤더 추가
        record.headers().add("eventId", envelope.eventId().getBytes());
        record.headers().add("eventType", envelope.eventType().getBytes());
        record.headers().add("correlationId", envelope.correlationId().getBytes());

        kafkaTemplate.send(record);
    }

}
