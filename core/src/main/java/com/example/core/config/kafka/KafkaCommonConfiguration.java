package com.example.core.config.kafka;

import com.example.core.logging.kafka.KafkaProducerLoggingListener;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

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
        log.info("KafkaTemplate started");
        return kafkaTemplate;
    }

    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>(properties.buildConsumerProperties());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // Key 에 대한 Deserializer 설정
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class); // Value 에 대한 Deserializer 설정
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
                    log.info("Sending record to DLQ. topic: {}, partition: {}, offset: {}, exception: {}, message: {}",
                            record.topic(), record.partition(), record.offset(), ex.getClass().getName(), ex.getMessage(), ex);
                    return new TopicPartition(record.topic() + ".dlq", record.partition());
                }
        );
    }

    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        log.info("DeadLetterPublishingRecoverer not implemented yet");
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(2000L, 3L));
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
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // 수동 커밋 모드 설정
        return factory;
    }

}
