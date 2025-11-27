package com.ll.core.logging.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaProducerLoggingListener<K, V> implements ProducerListener<K, V> {

    //Todo : 차후 이곳에 로깅 시스템을 넣으면 모니터링 가능.

    @Override
    public void onSuccess(ProducerRecord<K, V> producerRecord, RecordMetadata recordMetadata) {
        log.info(producerRecord.value().toString());
        log.info("[Kafka Produce Success] topic={}, key={}, partition={}, offset={}",
                producerRecord.topic(),
                producerRecord.key(),
                recordMetadata.partition(),
                recordMetadata.offset());
        log.info("time to produce={}ms", System.currentTimeMillis() - recordMetadata.timestamp());
    }

    @Override
    public void onError(ProducerRecord<K, V> producerRecord, RecordMetadata recordMetadata, Exception exception) {
        log.error("[Kafka Produce Error] topic={}, key={}, error={}",
                producerRecord.topic(),
                producerRecord.key(),
                exception.getMessage(),
                exception);

        log.info("time to produce={}ms", System.currentTimeMillis() - recordMetadata.timestamp());

    }
}

