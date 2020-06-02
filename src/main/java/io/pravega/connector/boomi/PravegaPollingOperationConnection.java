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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaPollingOperationConnection extends BaseConnection<OperationContext> implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(PravegaPollingOperationConnection.class.getName());

    private ReaderConfig readerConfig;
    //private AtomicBoolean isRunning = new AtomicBoolean(false);
    private EventStreamReader<String> reader = null;
    private EventStreamClientFactory clientFactory = null;

    /**
     * Creates a new instance using the provided operation context
     */
    public PravegaPollingOperationConnection(OperationContext context, String keycloakJSONPath) {
        super(context);
        readerConfig = new ReaderConfig(context, keycloakJSONPath);

        // create reader group
        PravegaUtil.createReaderGroup(readerConfig);
    }

    /**
     * Executing a request and converting the response to 1 or more {@link Payload} instances.
     *
     * @return the payloads
     */
    public Iterable<Payload> executePayloadRequest() {
        List eventList = new ArrayList<Payload>();

        TimeUnit time = TimeUnit.MILLISECONDS;
        long interval = time.convert(readerConfig.getInterval(), readerConfig.getUnit());
        long eventCounter = 0;
        long maxEventCounter = 100000; // Boomi can process 100k documents at a time
        long executionStartTime = System.currentTimeMillis();
        EventRead<String> event = null;
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

        return eventList;
    }

    /**
     * Simulates opening a connection to the API. Any operation specific connection initialization can occur here.
     */
    public void open() {
        try {
            clientFactory = PravegaUtil.createClientFactory(readerConfig);
            reader = PravegaUtil.createReader(readerConfig, clientFactory);
        } catch (Throwable t) {
            close();
            logger.log(Level.SEVERE, String.format("Error reading from %s/%s", readerConfig.getScope(), readerConfig.getStream()), t);
        }
    }

    /**
     * Closing the connection to the API. Any operation specific connection shut down can occur here.
     */

    @Override
    public void close() {
        if (reader != null) try {
            //logger.log(Level.INFO, String.format("READER CLOSE CALLED"));
            reader.close();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Could not close Pravega reader", t);
        }
    }

    @Override
    public void finalize() {
        //logger.log(Level.INFO, "finalize() called");
        close();
    }
}
