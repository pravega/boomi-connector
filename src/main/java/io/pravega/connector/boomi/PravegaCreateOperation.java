package io.pravega.connector.boomi;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaCreateOperation extends BaseUpdateOperation {
    private static final Logger logger = Logger.getLogger(PravegaCreateOperation.class.getName());

    private WriterConfig writerConfig;

    PravegaCreateOperation(OperationContext context) {
        super(context);
        writerConfig = new WriterConfig(context);
    }

    // caller must close
    private EventStreamWriter<String> createWriter(EventStreamClientFactory clientFactory) {
        return clientFactory.createEventWriter(
                writerConfig.getStream(), new UTF8StringSerializer(), EventWriterConfig.builder().build());
    }

    /**
     * Note: The Boomi execution process (which is a black box to us) does not have any mechanism to explicitly close
     * connectors or operation resources, and we should close the writer instance if it will no longer be used,
     * otherwise it may impact resources allocated on the server side. Therefore, we must explicitly close the writer
     * after every execution (we have no other choice).
     */
    @Override
    protected void executeUpdate(UpdateRequest request, OperationResponse response) {
        Logger logger = response.getLogger();

        try (EventStreamClientFactory clientFactory = PravegaUtil.createClientFactory(writerConfig);
             EventStreamWriter<String> writer = createWriter(clientFactory)) {
            List<CompletableFuture> futures = new ArrayList<>();
            for (ObjectData input : request) {
                try {
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
                } catch (Throwable t) {
                    // make best effort to process every input
                    // note: if we get here, something is very wrong and likely fatal, but we don't seem to have any control
                    //   over the overarching Boomi process; it's debatable whether to bubble an exception or continue
                    //   processing documents
                    ResponseUtil.addExceptionFailure(response, input, t);
                    logger.log(Level.SEVERE, "Unexpected error", t);
                }
            }

            // wait for writes to complete before returning; Boomi will complain if we don't generate a response for every
            // input document
            for (CompletableFuture future : futures) {
                // an exception here is likely fatal, so bubble it up
                future.join();
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
            logger.log(Level.WARNING, String.format("Error writing document %s", input.getTrackingId()), throwable);
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
}
