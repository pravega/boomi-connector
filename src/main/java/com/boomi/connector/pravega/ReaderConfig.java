package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;

import java.util.Map;
import java.util.UUID;

class ReaderConfig {
    static ReaderConfig fromContext(OperationContext context) {
        ReaderConfig readerConfig = new ReaderConfig();
        Map<String, Object> props = context.getOperationProperties();
        String readerGroup = (String) props.get(Constants.READER_GROUP_PROPERTY);
        if (readerGroup == null) readerGroup = 'R' + UUID.randomUUID().toString().replaceAll("-", "");
        readerConfig.setReaderGroup(readerGroup);
        readerConfig.setReadTimeout((long) props.get(Constants.READ_TIMEOUT_PROPERTY));
        return readerConfig;
    }

    private String readerGroup;
    private long readTimeout;

    public String getReaderGroup() {
        return readerGroup;
    }

    public void setReaderGroup(String readerGroup) {
        this.readerGroup = readerGroup;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }
}
