package com.boomi.serialization;

import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;

public class UTF8StringDeserializationSchema extends AbstractDeserializationSchema<String> {
    @Override
    public String deserialize(byte[] message) {
        return new String(message, StandardCharsets.UTF_8);
    }
}