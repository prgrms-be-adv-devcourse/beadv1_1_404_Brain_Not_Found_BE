package com.ll.core.config.kafka;

import com.ll.core.logging.kafka.KafkaProducerLoggingListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
@RequiredArgsConstructor
public class KafkaCommonConfiguration {

    @Value("${custom.config.kafka.acks:all}")
    private String ack;
    @Value("${custom.config.kafka.enable-idempotence:true}")
    private Boolean enableIdempotence;

    private final KafkaProperties properties;

    // Producer Configuration
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>(properties.buildProducerProperties());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class); // Key 에 대한 Serializer 설정
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // Value 에 대한 Serializer 설정
        config.put(ProducerConfig.ACKS_CONFIG, ack); // 메시지 전송 확인 설정 ( all -> 리더와 팔로워 모두 확인 )
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence); // 중복 방지 설정
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> factory,
            KafkaProducerLoggingListener<String, Object> listener
    ) {
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(factory);
        kafkaTemplate.setProducerListener(listener);
        kafkaTemplate.setObservationEnabled(true);
        log.info("KafkaTemplate started");
        return kafkaTemplate;
    }

    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>(properties.buildConsumerProperties());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // Key 에 대한 Deserializer 설정
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EnvelopeDeserializer.class); // Value 에 대한 Deserializer 설정+
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); // 모든 패키지 신뢰 설정
        return new DefaultKafkaConsumerFactory<>(config);
    }

    // DLQ Configuration

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        log.info("DeadLetterPublishingRecoverer started");
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    // Todo : DLQ 토픽 발생시 Slack Kafka DLQ Alert 구현 고려
                    log.info("DLQ Error 원인 : {}", ex.getCause().getMessage());
                    return new TopicPartition(record.topic() + ".dlq", record.partition());
                }
        );
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);

        // 1초, 2초, 4초, 8초, 16초, 최대 30초
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(30000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        handler.addNotRetryableExceptions(KafkaNotRetryableExceptionConfiguration.NOT_RETRYABLE_EXCEPTIONS);

        handler.setRetryListeners((record, ex, deliveryAttempt) ->
                // Todo : DeliveryAttempt 값이 일정 수준 이상일 때 Slack Kafka Retry Alert 구현 고려
                log.warn("Failed record in retry listener. topic: {}, partition: {}, offset: {}, exception: {}, message: {}, deliveryAttempt: {}",
                record.topic(), record.partition(), record.offset(), ex.getClass().getName(), ex.getMessage(), deliveryAttempt));

        handler.setCommitRecovered(true);

        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        log.info("ConcurrentKafkaListenerContainerFactory started");
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setObservationEnabled(true);
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // 수동 커밋 모드 설정
        return factory;
    }

}
