package io.pravega.connector.boomi;

import com.boomi.connector.api.*;
import com.boomi.connector.util.SizeLimitedUpdateOperation;
import com.jayway.jsonpath.JsonPath;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.impl.UTF8StringSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaWriteOperation extends SizeLimitedUpdateOperation {
    private WriterConfig writerConfig;

    PravegaWriteOperation(OperationContext context) {
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
    protected void executeSizeLimitedUpdate(UpdateRequest request, OperationResponse response) {
        Logger logger = response.getLogger();

        try (EventStreamClientFactory clientFactory = PravegaUtil.createClientFactory(writerConfig);
             EventStreamWriter<String> writer = createWriter(clientFactory)) {
            List<Future<ObjectData>> futures = new ArrayList<>();
            for (ObjectData input : request) {
                try {
                    String message = inputStreamToUtf8String(input.getData());
                    String routingKey = getRoutingKey(message, logger);

                    logger.log(Level.FINE, String.format("Writing message size: '%d' with routing-key: '%s' to stream '%s / %s'",
                            input.getDataSize(), routingKey, writerConfig.getScope(), writerConfig.getStream()));

                    // write the event
                    // note: this is an async call, so we will collect the futures and process the results later
                    if (routingKey != null && routingKey.length() > 0) {
                        futures.add(new ResultFuture<>(writer.writeEvent(routingKey, message), input));
                    } else {
                        futures.add(new ResultFuture<>(writer.writeEvent(message), input));
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

            // process all the futures and inform Boomi of all results
            for (Future<ObjectData> future : futures) {
                try {

                    // we wrapped Future<Void> from the writer with ResultFuture<ObjectData>, storing the input data as
                    // the result so we can grab it here
                    ObjectData input = future.get();
                    response.addResult(input, OperationStatus.SUCCESS, "OK", null,
                            ResponseUtil.toPayload(new ByteArrayInputStream(new byte[0])));
                } catch (ResultException e) {

                    // if there was a write exception, ResultFuture will wrap it in ResultException, so we can still
                    // grab the input data as the value
                    logger.log(Level.SEVERE, String.format("Error writing document %s", ((ObjectData) e.getValue()).getTrackingId()), e.getCause());
                    ResponseUtil.addExceptionFailure(response, (ObjectData) e.getValue(), e.getCause());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Unexpected exception during writer ack", e);
                }
            }
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

    private static String inputStreamToUtf8String(InputStream is) throws IOException {
        try (InputStream dataStream = is) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[16 * 1024];
            int c;
            while ((c = dataStream.read(buffer)) >= 0) {
                baos.write(buffer, 0, c);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    static class ResultFuture<V> implements Future<V> {
        private Future<Void> wrapped;
        private V value;

        ResultFuture(Future<Void> wrapped, V value) {
            this.wrapped = wrapped;
            this.value = value;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return wrapped.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return wrapped.isCancelled();
        }

        @Override
        public boolean isDone() {
            return wrapped.isDone();
        }

        @Override
        public V get() {
            try {
                wrapped.get();
                return value;
            } catch (Throwable t) {
                throw new ResultException(value, t);
            }
        }

        @Override
        public V get(long timeout, TimeUnit unit) {
            try {
                wrapped.get(timeout, unit);
                return value;
            } catch (Throwable t) {
                throw new ResultException(value, t);
            }
        }
    }

    static class ResultException extends RuntimeException {
        private Object value;

        public ResultException(Object value, Throwable cause) {
            super(cause);
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }
}
