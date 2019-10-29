package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;

import java.util.Map;

public class ReaderConfig {
    public static ReaderConfig fromContext(OperationContext context) {
        ReaderConfig readerConfig = new ReaderConfig();
        Map<String, Object> props = context.getOperationProperties();
        readerConfig.setReadTimeout((long) props.get(Constants.READTIMEOUT_PROPERTY));
        return readerConfig;
    }

    private long readTimeout;

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }
}
