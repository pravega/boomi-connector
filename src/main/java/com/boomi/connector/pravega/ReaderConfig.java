package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;

import java.util.Map;
import java.util.UUID;

class ReaderConfig {
    public static final long DEFAULT_READ_TIMEOUT = 2000; // ms
    public static final long DEFAULT_MAX_READ_TIME = 30; // seconds

    static ReaderConfig fromContext(OperationContext context) {
        ReaderConfig readerConfig = new ReaderConfig();
        Map<String, Object> props = context.getOperationProperties();
        String readerGroup = (String) props.get(Constants.READER_GROUP_PROPERTY);
        if (readerGroup == null) readerGroup = "boomi-reader-" + UUID.randomUUID().toString();
        readerConfig.setReaderGroup(readerGroup);
        readerConfig.setReadTimeout((long) props.getOrDefault(Constants.READ_TIMEOUT_PROPERTY, DEFAULT_READ_TIMEOUT));
        readerConfig.setMaxReadTimePerExecution((long) props.getOrDefault(Constants.MAX_READ_TIME_PER_EXECUTION_PROPERTY, DEFAULT_MAX_READ_TIME));
        return readerConfig;
    }

    private String readerGroup;
    private long readTimeout;
    private long maxReadTimePerExecution;

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

    public long getMaxReadTimePerExecution() {
        return maxReadTimePerExecution;
    }

    public void setMaxReadTimePerExecution(long maxReadTimePerExecution) {
        this.maxReadTimePerExecution = maxReadTimePerExecution;
    }
}
