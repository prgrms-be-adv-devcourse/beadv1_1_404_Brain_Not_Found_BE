package com.ll.products.domain.recommendation.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class QdrantConfig {

    @Value("${qdrant.host}")
    private String host;

    @Value("${qdrant.port}")
    private int port;

    @Value("${qdrant.collection-name}")
    private String collectionName;


    @Bean
    public QdrantClient qdrantClient() {
        log.info("Qdrant 클라이언트 초기화: {}:{}", host, port);
        try {
            return new QdrantClient(
                    QdrantGrpcClient.newBuilder(host, port, false)
                            .withTimeout(Duration.ofSeconds(10))
                            .build()
            );
        } catch (Exception e) {
            log.error("Qdrant 클라이언트 초기화 실패", e);
            throw new IllegalStateException("Qdrant 연결 불가", e);
        }
    }

    @Bean
    public String qdrantCollectionName() {
        return collectionName;
    }
}