package com.ll.core.config.kafka;

import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.application.name:unknown-module}")
    private String moduleName;

    public <T> void publish(String topic, T payload) {
        KafkaEventEnvelope<T> envelope = KafkaEventEnvelope.wrap(moduleName, payload);

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, envelope);

        // 분석용 중복 헤더 추가
        record.headers().add("eventId", envelope.eventId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("eventType", envelope.eventType().getBytes(StandardCharsets.UTF_8));

        kafkaTemplate.send(record);
    }

}
