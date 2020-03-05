package io.pravega.connector.boomi;

import com.boomi.connector.api.ConnectorException;
import com.boomi.connector.api.OperationContext;

import java.util.Map;

class ReaderConfig extends PravegaConfig {
    public static final long DEFAULT_READ_TIMEOUT = 2000; // ms
    public static final long DEFAULT_MAX_READ_TIME = 30; // seconds
    public static final long DEFAULT_MAX_EVENTS = 100000; // events

    private String readerGroup;
    private long readTimeout;
    private long maxReadTimePerExecution;
    private long maxEventsPerExecution;
    private InitialReaderPosition initialReaderPosition;

    public ReaderConfig() {
    }

    public ReaderConfig(OperationContext context, String keycloakJsonPath) {
        super(context, keycloakJsonPath);
        Map<String, Object> props = context.getOperationProperties();

        // reader group should always be set
        String readerGroup = (String) props.get(Constants.READER_GROUP_PROPERTY);
        if (readerGroup == null || readerGroup.trim().length() == 0)
            throw new ConnectorException("Reader Group must be set");

        setReaderGroup(readerGroup);
        setReadTimeout((long) getOrDefault(props, Constants.READ_TIMEOUT_PROPERTY, DEFAULT_READ_TIMEOUT));
        setMaxReadTimePerExecution((long) getOrDefault(props, Constants.MAX_READ_TIME_PER_EXECUTION_PROPERTY, DEFAULT_MAX_READ_TIME));
        setMaxEventsPerExecution((long) getOrDefault(props, Constants.MAX_EVENTS_PER_EXECUTION_PROPERTY, DEFAULT_MAX_EVENTS));
        String initialReaderPosition = (String) props.get(Constants.INITIAL_READER_GROUP_POSITION);
        if (initialReaderPosition != null)
            setInitialReaderPosition(InitialReaderPosition.valueOf(initialReaderPosition));
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

    public long getMaxEventsPerExecution() {
        return maxEventsPerExecution;
    }

    public void setMaxEventsPerExecution(long maxEventsPerExecution) {
        this.maxEventsPerExecution = maxEventsPerExecution;
    }

    public InitialReaderPosition getInitialReaderPosition() {
        return initialReaderPosition;
    }

    public void setInitialReaderPosition(InitialReaderPosition initialReaderPosition) {
        this.initialReaderPosition = initialReaderPosition;
    }

    enum InitialReaderPosition {Head, Tail}
}
