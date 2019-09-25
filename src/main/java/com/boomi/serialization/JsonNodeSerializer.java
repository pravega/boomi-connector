package com.boomi.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.pravega.client.stream.Serializer;

public class JsonNodeSerializer<T> implements Serializer<T> {

    private final ObjectMapper objectMapper;
    private Class<T> valueType;

    public JsonNodeSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    public JsonNodeSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ByteBuffer serialize(T value) {
        try {
            byte[] result = objectMapper.writeValueAsBytes(value);
            return ByteBuffer.wrap(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T deserialize(ByteBuffer serializedValue) {
        ByteArrayInputStream bin = new ByteArrayInputStream(serializedValue.array(),
                serializedValue.position(),
                serializedValue.remaining());
        try {
                      return objectMapper.readValue(objectMapper.readTree(bin).asText(), valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}