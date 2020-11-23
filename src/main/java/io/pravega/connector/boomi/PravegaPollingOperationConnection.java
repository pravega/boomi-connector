/*
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

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
        logger.log(Level.FINE, String.format("Reading events from %s/%s", readerConfig.getScope(), readerConfig.getStream()));
        do {
            try {
                event = reader.readNextEvent(readerConfig.getReadTimeout());
                if (event.getEvent() != null) {
                    logger.log(Level.FINEST, String.format("Listener Read event size: %d", event.getEvent().length()));
                    eventList.add(PayloadUtil.toPayload(event.getEvent()));
                    eventCounter++;
                } // could check for watermark or checkpoint in else block
            } catch (ReinitializationRequiredException e) {
                // There are certain circumstances where the reader needs to be reinitialized
                logger.log(Level.WARNING, "Caught ReinitializationRequiredException - Pravega client needs to be reinitialized", e);
                closeReader();
                reader = PravegaUtil.createReader(readerConfig, clientFactory);
            } catch (TruncatedDataException e) {
                // Assuming nothing needs to be done here and that the next call to readNextEvent() will return the next event
                logger.log(Level.WARNING, "Caught TruncatedDataException", e);
            }
            // keep looping until there is no event or checkpoint, already run longer than interval, hitting maximum number of events
        } while ((event.getEvent() != null || event.isCheckpoint())
                && interval > 0
                && (System.currentTimeMillis() - executionStartTime < interval)
                && eventCounter < maxEventCounter);

        return eventList;
    }

    /**
     * Simulates opening a connection to the API. Any operation specific connection initialization can occur here.
     */
    public synchronized void open() {
        try {
            if (clientFactory == null)
                clientFactory = PravegaUtil.createClientFactory(readerConfig);
            if (reader == null)
                reader = PravegaUtil.createReader(readerConfig, clientFactory);
        } catch (Exception e) {
            close();
            logger.log(Level.SEVERE, String.format("Error reading from %s/%s", readerConfig.getScope(), readerConfig.getStream()), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Closing the connection to the API. Any operation specific connection shut down can occur here.
     */

    @Override
    public synchronized void close() {
        closeReader();
        if (clientFactory != null) try {
            clientFactory.close();
            clientFactory = null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not close Pravega clientFactory", e);
        }
    }

    private synchronized void closeReader() {
        if (reader != null) try {
            reader.close();
            reader = null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not close Pravega reader", e);
        }
    }

    @Override
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
