// Copyright (c) 2019 Boomi, Inc.
package io.pravega.connector.boomi;

import com.boomi.connector.api.OperationContext;
import com.boomi.connector.api.Payload;
import com.boomi.connector.api.PayloadUtil;
import com.boomi.connector.util.BaseConnection;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.TruncatedDataException;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PollingOperationConnection extends BaseConnection<OperationContext> implements Closeable {

    private static final Logger logger = Logger.getLogger(PravegaListenOperation.class.getName());

    private ReaderConfig readerConfig;
    //private AtomicBoolean isRunning = new AtomicBoolean(false);
    private EventStreamReader<String> reader = null;

    /**
     * Creates a new instance using the provided operation context
     */
    public PollingOperationConnection(OperationContext context) {
        super(context);
        readerConfig = new ReaderConfig(context);

        // create reader group
        PravegaUtil.createReaderGroup(readerConfig);
    }

    /**
     * Executing a request and converting the response to 1 or more {@link Payload} instances.
     *
     * @return the payloads
     */
    public Iterable<Payload> executePayloadRequest() {
        List eventList = null;
        try (EventStreamClientFactory clientFactory = PravegaUtil.createClientFactory(readerConfig)) {

            reader = PravegaUtil.createReader(readerConfig, clientFactory);
            long interval = readerConfig.getInterval() * 1000;
            long eventCounter = 0;
            long maxEventCounter = 100000; // Boomi can process 100k documents at a time
            long executionStartTime = System.currentTimeMillis();
            EventRead<String> event = null;
            eventList = new ArrayList<Payload>();
            logger.info(String.format("Reading events from %s/%s", readerConfig.getScope(), readerConfig.getStream()));
            do {
                try {
                    event = reader.readNextEvent(readerConfig.getReadTimeout());
                    if (event.getEvent() != null) {
                        logger.log(Level.FINE, String.format("Listener Read event size: %d", event.getEvent().length()));
                        //listener.submit(PayloadUtil.toPayload(event.getEvent()));
                        eventList.add(PayloadUtil.toPayload(event.getEvent()));
                        eventCounter++;
                    } // could check for watermark or checkpoint in else block
                } catch (ReinitializationRequiredException e) {
                    // There are certain circumstances where the reader needs to be reinitialized
                    logger.log(Level.WARNING, "Caught ReinitializationRequiredException - Pravega client needs to be reinitialized", e);
                    close();
                    reader = PravegaUtil.createReader(readerConfig, clientFactory);
                } catch (TruncatedDataException e) {
                    // Assuming nothing needs to be done here and that the next call to readNextEvent() will return the next event
                    logger.log(Level.WARNING, "Caught TruncatedDataException", e);
                }
                // keep looping until we are stopped
            } while ((event.getEvent() != null || event.isCheckpoint())
                    && interval > 0
                    && (System.currentTimeMillis() - executionStartTime < interval)
                    && eventCounter < maxEventCounter);

            // make sure we close the reader before the client is closed, otherwise it seems the reader is not properly
            // removed from the reader group and may starve other readers in that group (i.e. in subsequent executions)
            close();
        } catch (Throwable t) {
            close();
            logger.log(Level.SEVERE, String.format("Error reading from %s/%s", readerConfig.getScope(), readerConfig.getStream()), t);
        }

        return eventList;
    }

    /**
     * Simulates opening a connection to the API. Any operation specific connection initialization can occur here. Since
     * this is an example, it does nothing.
     */
    public void open() {

    }

    /**
     * Closing the connection to the API. Any operation specific connection shut down can occur here.
     */

    @Override
    public void close() {
        if (reader != null) try {
            reader.close();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Could not close Pravega reader", t);
        }
    }
}
