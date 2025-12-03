package com.ll.core.model.vo.kafka;


import jakarta.validation.Valid;

import java.util.UUID;

public record KafkaEventEnvelope<T>(
        String eventId,          // 멱등성 키
        String eventType,        // 이벤트 종류
        int eventVersion,        // 스키마 버전
        long timestamp,          // 이벤트 발생 시간
        String producerService,  // 서비스 이름
        String payloadType,      // 페이로드 타입
        @Valid
        T payload                // 실제 비즈니스 데이터
) {

    public static <T> KafkaEventEnvelope<T> wrap(
            String producerService,
            T payload
    ) {
        return new KafkaEventEnvelope<>(
                UUID.randomUUID().toString(),
                payload.getClass().getSimpleName(),
                1,
                System.currentTimeMillis(),
                producerService,
                payload.getClass().getName(),
                payload
        );
    }
}