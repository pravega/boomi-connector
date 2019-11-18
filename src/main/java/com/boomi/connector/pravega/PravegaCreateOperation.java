package com.boomi.connector.pravega;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseUpdateOperation;
import com.jayway.jsonpath.JsonPath;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.impl.UTF8StringSerializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaCreateOperation extends BaseUpdateOperation implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(PravegaCreateOperation.class.getName());

    private WriterConfig writerConfig;
    private EventStreamClientFactory clientFactory;
    private EventStreamWriter<String> writer;
    private AtomicBoolean closed = new AtomicBoolean(false);

    PravegaCreateOperation(OperationContext context) {
        super(context);
        writerConfig = new WriterConfig(context);

        // create client factory
        // TODO: find an appropriate way to cache client factories
        clientFactory = PravegaUtil.createClientFactory(writerConfig);

        // create event writer
        writer = clientFactory.createEventWriter(
                writerConfig.getStream(), new UTF8StringSerializer(), EventWriterConfig.builder().build());
    }

    @Override
    protected void executeUpdate(UpdateRequest request, OperationResponse response) {
        Logger logger = response.getLogger();

        List<CompletableFuture> futures = new ArrayList<>();
        for (ObjectData input : request) {
            try {
                if (closed.get()) throw new IllegalStateException("This operation instance has been closed");

                String message = inputStreamToString(input.getData());
                String routingKey = getRoutingKey(message, logger);

                logger.log(Level.INFO, String.format("Writing message size: '%d' with routing-key: '%s' to stream '%s / %s'%n",
                        input.getDataSize(), routingKey, writerConfig.getScope(), writerConfig.getStream()));

                // write the event
                // note: this is an async call, so we will add an additional async completion stage to the call,
                // which will handle the result, whether it was successful or not
                if (routingKey != null && routingKey.length() > 0) {
                    futures.add(writer.writeEvent(routingKey, message)
                            .whenCompleteAsync((aVoid, throwable) -> handleResult(input, response, throwable)));
                } else {
                    futures.add(writer.writeEvent(message)
                            .whenCompleteAsync((aVoid, throwable) -> handleResult(input, response, throwable)));
                }
            } catch (Exception e) {
                // make best effort to process every input
                // TODO: if Boomi retries this document, it might be inserted out of order.. if it does not retry, then
                // the data might be lost - how can we guarantee order and exactly-once?
                ResponseUtil.addExceptionFailure(response, input, e);
            }
        }

        // wait for writes to complete before returning; not sure how Boomi would handle returning here with unfinished
        // writer threads
        for (CompletableFuture future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                // ignore (this is only to join the writer threads; we already dealt with any exceptions)
            }
        }
    }

    private void handleResult(ObjectData input, OperationResponse response, Throwable throwable) {
        if (throwable == null) {
            // dump the results into the response
            response.addResult(input, OperationStatus.SUCCESS, "OK",
                    "OK", ResponseUtil.toPayload(new ByteArrayInputStream(new byte[0])));
        } else {
            ResponseUtil.addExceptionFailure(response, input, throwable);
        }
    }

    private String getRoutingKey(String message, Logger logger) {
        try {
            String routingKey;
            if (WriterConfig.RoutingKeyType.JsonReference == writerConfig.getRoutingKeyType()) {
                routingKey = JsonPath.read(message, writerConfig.getRoutingKey());
            } else {
                routingKey = writerConfig.getRoutingKey();
            }
            return routingKey;
        } catch (Exception e) {
            logger.warning("could not parse routing key: " + e);
            return "";
        }
    }

    private static String inputStreamToString(InputStream is) {
        try (Scanner scanner = new Scanner(is, "UTF-8")) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    // Idempotent
    // Exception free
    @Override
    public synchronized void close() {
        if (closed.compareAndSet(false, true)) {
            if (writer != null) try {
                writer.close();
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Could not close Pravega writer", t);
            }
            writer = null;
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
