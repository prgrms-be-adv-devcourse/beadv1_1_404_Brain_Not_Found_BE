package com.ll.core.config.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.core.model.vo.kafka.KafkaEventEnvelope;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class EnvelopDeserializer implements Deserializer<KafkaEventEnvelope<?>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public KafkaEventEnvelope<?> deserialize(String topic, byte[] data) {
        try {
            JsonNode jsonNode = objectMapper.readTree(data);

            String payloadType = jsonNode.get("payloadType").asText();
            JsonNode payload = jsonNode.get("payload");

            Class<?> clazz = Class.forName(payloadType);
            Object payloadObject = objectMapper.treeToValue(payload, clazz);

            return new KafkaEventEnvelope<>(
                    jsonNode.get("eventId").asText(),
                    jsonNode.get("eventType").asText(),
                    jsonNode.get("eventVersion").asInt(),
                    jsonNode.get("timestamp").asLong(),
                    jsonNode.get("producerService").asText(),
                    jsonNode.get("correlationId").asText(),
                    payloadType,
                    payloadObject
            );
        } catch ( Exception e ) {
            throw new SerializationException("Failed to deserialize envelope", e);
        }
    }
}
