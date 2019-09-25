package com.boomi.serialization;

import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;

import com.boomi.model.ImageData;

@SuppressWarnings("serial")
public class ByteArrayDeserializationSchema extends AbstractDeserializationSchema<ImageData> {

    @Override
    public ImageData deserialize(byte[] message) {
        //ByteBuffer buf = ByteBuffer.wrap(payload);
        return null;
    }

}
