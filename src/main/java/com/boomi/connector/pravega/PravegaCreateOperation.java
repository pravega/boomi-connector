package com.boomi.connector.pravega;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseUpdateOperation;
import com.jayway.jsonpath.JsonPath;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.impl.JavaSerializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaCreateOperation extends BaseUpdateOperation implements AutoCloseable {
    private EventStreamWriter<String> writer;
    private PravegaConfig pravegaConfig;
    private WriterConfig writerConfig;

    PravegaCreateOperation(PravegaConnection conn) {
		super(conn);
        pravegaConfig = conn.getPravegaConfig();
        writerConfig = WriterConfig.fromContext(this.getContext());

        // create event writer
        writer = conn.getClientFactory().createEventWriter(
                pravegaConfig.getStreamName(), new JavaSerializer<>(), EventWriterConfig.builder().build());
    }

	@Override
	protected void executeUpdate(UpdateRequest request, OperationResponse response) {
    	Logger logger = response.getLogger();

        for (ObjectData input : request) {
            try {
            	String message = inputStreamToString(input.getData());
                String routingKey = getRoutingKey(message, logger);

                logger.log(Level.INFO, String.format("Writing message size: '%d' with routing-key: '%s' to stream '%s / %s'%n",
                        input.getDataSize(), routingKey, pravegaConfig.getScope(), pravegaConfig.getStreamName()));

                // write the event
                // note: this is an async call, so we will add an additional async completion stage to the call,
                // which will handle the result, whether it was successful or not
                if (routingKey != null && routingKey.length() > 0) {
                    writer.writeEvent(routingKey, message)
                            .whenCompleteAsync((aVoid, throwable) -> handleResult(input, response, throwable));
                } else {
                    writer.writeEvent(message)
                            .whenCompleteAsync((aVoid, throwable) -> handleResult(input, response, throwable));
                }
            } catch (Exception e) {
                // make best effort to process every input
                ResponseUtil.addExceptionFailure(response, input, e);
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

    @Override
    public void close() {
        if (writer != null) writer.close();
    }

    @Override
    public PravegaConnection getConnection() {
        return (PravegaConnection) super.getConnection();
    }

    static String inputStreamToString(InputStream is) {
    	try (Scanner scanner = new Scanner(is, "UTF-8")) {
    		return scanner.useDelimiter("\\A").next();
    	}
    }

    private String getRoutingKey(String message, Logger logger) {
        try {
            String routingKey;
            if (writerConfig.isRoutingKeyNeeded()) {
                routingKey = JsonPath.read(message, writerConfig.getRoutingKeyConfigValue());
            } else {
                routingKey = writerConfig.getFixedRoutingKey();
            }
            return routingKey;
        }
        catch (Exception e) {
            logger.warning("could not parse routing key: " + e);
            return "";
        }
    }
}
