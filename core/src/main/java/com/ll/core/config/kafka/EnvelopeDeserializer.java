package com.ll.core.config.kafka;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnvelopeDeserializer implements Deserializer<KafkaEventEnvelope<?>> {
    private static class EnvelopeFields {
        String eventId;
        String eventType;
        int eventVersion;
        long timestamp;
        String producerService;
        String payloadType;
        Object payload;
    }

    private final JsonFactory jsonFactory = new JsonFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>(); // 로컬 캐시를 이용해 성능 최적화

    @Override
    public KafkaEventEnvelope<?> deserialize(String topic, byte[] data) {
        if ( data == null || data.length == 0 ) return null;

        EnvelopeFields f = new EnvelopeFields();

        try ( JsonParser parser = jsonFactory.createParser(data) ) {
            ensureStartObject(parser);
            parseEnvelopeFields(parser, f);

            return new KafkaEventEnvelope<>(
                    f.eventId,
                    f.eventType,
                    f.eventVersion,
                    f.timestamp,
                    f.producerService,
                    f.payloadType,
                    f.payload
            );
        } catch ( Exception e ) {
            throw new SerializationException("역직렬화 중 오류가 발생했습니다", e);
        }

    }

    private void ensureStartObject(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) {
            throw new SerializationException("역직렬화 중 오류가 발생했습니다");
        }
    }

    private void parseEnvelopeFields(JsonParser parser, EnvelopeFields f) throws IOException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken(); // move to value
            handleField(fieldName, parser, f);
        }
    }

    private void handleField(String fieldName, JsonParser parser, EnvelopeFields f) throws IOException {
        switch (fieldName) {
            case "eventId" -> f.eventId = parser.getValueAsString();
            case "eventType" -> f.eventType = parser.getValueAsString();
            case "eventVersion" -> f.eventVersion = parser.getIntValue();
            case "timestamp" -> f.timestamp = parser.getLongValue();
            case "producerService" -> f.producerService = parser.getValueAsString();
            case "payloadType" -> f.payloadType = parser.getValueAsString();
            case "payload" -> f.payload = getObject(f.payloadType, parser);
            default -> parser.skipChildren();
        }
    }

    private Object getObject(String payloadType, JsonParser parser) throws IOException {
        if ( payloadType == null ) {
            throw new SerializationException("payloadType must be read before payload");
        }
        return objectMapper.readValue(parser, resolvePayloadClass(payloadType));
    }

    private Class<?> resolvePayloadClass(String payloadType) {
        return CLASS_CACHE.computeIfAbsent(payloadType, key -> {
            try {
                return Class.forName(key);
            } catch (ClassNotFoundException e) {
                throw new SerializationException("Unknown payloadType: " + key, e);
            }
        });
    }
}
