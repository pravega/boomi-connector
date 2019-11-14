package com.boomi.connector.pravega;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseQueryOperation;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.JavaSerializer;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaQueryOperation extends BaseQueryOperation implements AutoCloseable {
    private PravegaConfig pravegaConfig;
    private ReaderConfig readerConfig;
    private EventStreamReader<String> reader;

    PravegaQueryOperation(PravegaConnection conn) {
        super(conn);
        pravegaConfig = conn.getPravegaConfig();
        readerConfig = ReaderConfig.fromContext(this.getContext());

        // create reader group
        ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(pravegaConfig.getScope(), pravegaConfig.getStream())).build();
        try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(pravegaConfig.getScope(), conn.getClientConfig())) {
            readerGroupManager.createReaderGroup(readerConfig.getReaderGroup(), readerGroupConfig);
        }

        // create event reader
        reader = conn.getClientFactory().createReader(UUID.randomUUID().toString(), readerConfig.getReaderGroup(),
                new JavaSerializer<>(), io.pravega.client.stream.ReaderConfig.builder().build());
    }

    @Override
    protected void executeQuery(QueryRequest request, OperationResponse response) {
        Logger logger = response.getLogger();
        FilterData input = request.getFilter();
        long executionStartTime = System.currentTimeMillis();
        long eventCounter = 0;

        try {
            logger.info(String.format("Reading events from %s/%s", pravegaConfig.getScope(), pravegaConfig.getStream()));
            EventRead<String> event = null;
            long maxDuration = readerConfig.getMaxReadTimePerExecution() * 1000; // convert to ms
            do {
                try {
                    event = reader.readNextEvent(readerConfig.getReadTimeout());
                    if (event.getEvent() != null) {
                        eventCounter++;
                        logger.log(Level.FINE, String.format("Read event size: %d", event.getEvent().length()));
                        response.addPartialResult(input, OperationStatus.SUCCESS, "OK",
                                "OK", ResponseUtil.toPayload(new ByteArrayInputStream(event.getEvent().getBytes())));
                    }
                } catch (ReinitializationRequiredException e) {
                    // There are certain circumstances where the reader needs to be reinitialized
                    // TODO: what needs to be done here?  if we re-initialize, how do we resume?
                    logger.log(Level.WARNING, "Caught ReinitializationRequiredException - Pravega client needs to be reinitialized", e);
                }
                // keep looping as long as:
                // - an event was read OR we hit a checkpoint
                //   AND
                // - maximum execution time has been set (greater than 0) AND our execution time is under that
            } while ((event.getEvent() != null || event.isCheckpoint())
                    && (maxDuration > 0 && System.currentTimeMillis() - executionStartTime < maxDuration));

            if (event.getEvent() == null)
                logger.log(Level.INFO, String.format("No more events from %s/%s: exiting", pravegaConfig.getScope(), pravegaConfig.getStream()));
            else
                logger.log(Level.INFO, String.format("Hit maximum read time (start: %d ms, now: %d ms, max: %d seconds): exiting",
                        executionStartTime, System.currentTimeMillis(), readerConfig.getMaxReadTimePerExecution()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Error reading from %s/%s", pravegaConfig.getScope(), pravegaConfig.getStream()), e);
            ResponseUtil.addExceptionFailure(response, input, e);
        }

        logger.log(Level.INFO, String.format("Read %d events in %d ms", eventCounter, System.currentTimeMillis() - executionStartTime));

        response.finishPartialResult(input);
    }

    @Override
    public void close() {
        if (reader != null) reader.close();
    }

    @Override
    public PravegaConnection getConnection() {
        return (PravegaConnection) super.getConnection();
    }
}
