package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;

import java.util.Map;
import java.util.UUID;

class ReaderConfig extends PravegaConfig {
    public static final long DEFAULT_READ_TIMEOUT = 2000; // ms
    public static final long DEFAULT_MAX_READ_TIME = 30; // seconds

    private String readerGroup;
    private long readTimeout;
    private long maxReadTimePerExecution;

    public ReaderConfig() {
    }

    public ReaderConfig(OperationContext context) {
        super(context);
        Map<String, Object> props = context.getOperationProperties();
        String readerGroup = (String) props.get(Constants.READER_GROUP_PROPERTY);
        if (readerGroup == null) readerGroup = "boomi-reader-" + UUID.randomUUID().toString();
        setReaderGroup(readerGroup);
        setReadTimeout((long) props.getOrDefault(Constants.READ_TIMEOUT_PROPERTY, DEFAULT_READ_TIMEOUT));
        setMaxReadTimePerExecution((long) props.getOrDefault(Constants.MAX_READ_TIME_PER_EXECUTION_PROPERTY, DEFAULT_MAX_READ_TIME));
    }

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
