package com.boomi.connector.pravega;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseQueryOperation;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.impl.UTF8StringSerializer;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaQueryOperation extends BaseQueryOperation implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(PravegaQueryOperation.class.getName());

    private ReaderConfig readerConfig;
    private EventStreamClientFactory clientFactory;
    private EventStreamReader<String> reader;
    private AtomicBoolean closed = new AtomicBoolean(false);

    PravegaQueryOperation(OperationContext context) {
        super(context);
        readerConfig = new ReaderConfig(context);

        // create client factory
        // TODO: find an appropriate way to cache client factories while facilitating cleanup
        //  (this instance is not closed automatically)
        clientFactory = PravegaUtil.createClientFactory(readerConfig);

        // create reader group
        PravegaUtil.createReaderGroup(readerConfig);

        // create event reader
        reader = clientFactory.createReader(UUID.randomUUID().toString(), readerConfig.getReaderGroup(),
                new UTF8StringSerializer(), io.pravega.client.stream.ReaderConfig.builder().build());
    }

    @Override
    protected void executeQuery(QueryRequest request, OperationResponse response) {
        Logger logger = response.getLogger();
        FilterData input = request.getFilter();
        long executionStartTime = System.currentTimeMillis();
        long eventCounter = 0;

        try {
            if (closed.get()) throw new IllegalStateException("This operation instance has been closed");

            logger.info(String.format("Reading events from %s/%s", readerConfig.getScope(), readerConfig.getStream()));
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
                logger.log(Level.INFO, String.format("No more events from %s/%s: exiting", readerConfig.getScope(), readerConfig.getStream()));
            else
                logger.log(Level.INFO, String.format("Hit maximum read time (start: %d ms, now: %d ms, max: %d seconds): exiting",
                        executionStartTime, System.currentTimeMillis(), readerConfig.getMaxReadTimePerExecution()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, String.format("Error reading from %s/%s", readerConfig.getScope(), readerConfig.getStream()), e);
            ResponseUtil.addExceptionFailure(response, input, e);
        }

        logger.log(Level.INFO, String.format("Read %d events in %d ms", eventCounter, System.currentTimeMillis() - executionStartTime));

        response.finishPartialResult(input);
    }

    // Idempotent
    // Exception free
    @Override
    public synchronized void close() {
        if (closed.compareAndSet(false, true)) {
            if (reader != null) try {
                reader.close();
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Could not close Pravega reader", t);
            }
            reader = null;
            if (clientFactory != null) try {
                clientFactory.close();
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Could not close Pravega client factory", t);
            }
            clientFactory = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
