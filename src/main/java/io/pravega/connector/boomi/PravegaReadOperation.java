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

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseQueryOperation;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.TruncatedDataException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaReadOperation extends BaseQueryOperation {
    private static final Logger logger = Logger.getLogger(PravegaReadOperation.class.getName());

    private ReaderConfig readerConfig;

    PravegaReadOperation(OperationContext context, String keycloakJSONPath) {
        super(context);
        readerConfig = new ReaderConfig(context, keycloakJSONPath);

        // create reader group
        PravegaUtil.createReaderGroup(readerConfig);
    }

    /**
     * Note: The Boomi execution process (which is a black box to us) does not have any mechanism to explicitly close
     * connectors or operation resources, and we *must* close the reader instance if it will no longer be used,
     * otherwise it will continue to own the segments assigned by the reader group and starve all other readers of the
     * stream. Therefore, we must explicitly close the reader after every execution (we have no other choice).
     */
    @Override
    protected void executeQuery(QueryRequest request, OperationResponse response) {
        Logger logger = response.getLogger();
        FilterData input = request.getFilter();
        long executionStartTime = System.currentTimeMillis();
        long eventCounter = 0;

        EventStreamReader<String> reader = null;
        EventStreamClientFactory clientFactory = null;
        try {
            clientFactory = PravegaUtil.createClientFactory(readerConfig);
            reader = PravegaUtil.createReader(readerConfig, clientFactory);

            logger.info(String.format("Reading events from %s/%s", readerConfig.getScope(), readerConfig.getStream()));
            EventRead<String> event = null;
            long maxDuration = readerConfig.getMaxReadTimePerExecution() * 1000; // convert to ms
            long maxEvents = readerConfig.getMaxEventsPerExecution();
            do {
                try {
                    event = reader.readNextEvent(readerConfig.getReadTimeout());
                    if (event.getEvent() != null) {
                        eventCounter++;
                        logger.log(Level.FINE, String.format("Read event size: %d", event.getEvent().length()));
                        response.addPartialResult(input, OperationStatus.SUCCESS, "OK", null,
                                PayloadUtil.toPayload(event.getEvent()));
                    }
                } catch (ReinitializationRequiredException e) {
                    // There are certain circumstances where the reader needs to be reinitialized
                    logger.log(Level.WARNING, "Caught ReinitializationRequiredException - Pravega client needs to be reinitialized", e);
                    close(reader);
                    reader = PravegaUtil.createReader(readerConfig, clientFactory);
                } catch (TruncatedDataException e) {
                    // Assuming nothing needs to be done here and that the next call to readNextEvent() will return the next event
                    logger.log(Level.WARNING, "Caught TruncatedDataException", e);
                }
                // keep looping as long as:
                // - an event was read OR we hit a checkpoint
                //   AND
                // - our execution time is under the maximum execution time (if set)
                //   AND
                // - number of events read is less than the maximum events to read (if set)
            } while ((event.getEvent() != null || event.isCheckpoint())
                    && (maxDuration <= 0 || System.currentTimeMillis() - executionStartTime < maxDuration)
                    && (maxEvents <= 0 || eventCounter < maxEvents));

            if (event.getEvent() == null)
                logger.log(Level.INFO, String.format("No more events from %s/%s: exiting", readerConfig.getScope(), readerConfig.getStream()));
            else if (maxEvents > 0 && eventCounter >= maxEvents)
                logger.log(Level.INFO, String.format("Hit maximum event count(read: %d, max: %d): exiting", eventCounter, maxEvents));
            else
                logger.log(Level.INFO, String.format("Hit maximum read time (start: %d ms, now: %d ms, max: %d seconds): exiting",
                        executionStartTime, System.currentTimeMillis(), readerConfig.getMaxReadTimePerExecution()));

            logger.log(Level.INFO, String.format("Read %d events in %d ms", eventCounter, System.currentTimeMillis() - executionStartTime));

            if (eventCounter > 0) response.finishPartialResult(input);
            else response.addEmptyResult(input, OperationStatus.SUCCESS, "OK", null);
        } finally {
            // make sure we close the reader before the client is closed, otherwise it seems the reader is not properly
            // removed from the reader group and may starve other readers in that group (i.e. in subsequent executions)
            close(reader);
            if (clientFactory != null) {
                try {
                    clientFactory.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not close Pravega client factory", e);
                }
            }
        }
    }

    // Exception free
    private void close(EventStreamReader<?> reader) {
        if (reader != null) try {
            reader.close();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not close Pravega reader", e);
        }
    }
}
