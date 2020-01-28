package io.pravega.connector.boomi;

import com.boomi.connector.api.OperationContext;
import com.boomi.connector.api.ResponseUtil;
import com.boomi.connector.api.listen.Listener;
import com.boomi.connector.util.listen.UnmanagedListenOperation;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.TruncatedDataException;
import io.pravega.client.stream.impl.UTF8StringSerializer;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaListenOperation extends UnmanagedListenOperation {

    private static final Logger logger = Logger.getLogger(PravegaListenOperation.class.getName());

    private ReaderConfig readerConfig;
    private Boolean isRunning = false;

    protected PravegaListenOperation(OperationContext context) {
        super(context);
        readerConfig = new ReaderConfig(context);

        // create reader group
        PravegaUtil.createReaderGroup(readerConfig);
    }

    // caller must close
    private EventStreamReader<String> createReader(EventStreamClientFactory clientFactory) {
        String readerId = UUID.randomUUID().toString();
        logger.info(String.format("Creating reader for stream %s / %s using group %s and ID %s",
                readerConfig.getScope(), readerConfig.getStream(), readerConfig.getReaderGroup(), readerId));
        return clientFactory.createReader(readerId, readerConfig.getReaderGroup(),
                new UTF8StringSerializer(), io.pravega.client.stream.ReaderConfig.builder().build());
    }

    @Override
    protected void start(Listener listener) {
        isRunning = true;
        long executionStartTime = System.currentTimeMillis();
        long eventCounter = 0;

        EventStreamReader<String> reader = null;
        try (EventStreamClientFactory clientFactory = PravegaUtil.createClientFactory(readerConfig)) {
            reader = createReader(clientFactory);

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

                        listener.submit(ResponseUtil.toPayload(event.getEvent()));

                        //response.addPartialResult(input, OperationStatus.SUCCESS, "OK", null,
                        //ResponseUtil.toPayload(new ByteArrayInputStream(event.getEvent().getBytes())));
                    }
                } catch (ReinitializationRequiredException e) {
                    // There are certain circumstances where the reader needs to be reinitialized
                    logger.log(Level.WARNING, "Caught ReinitializationRequiredException - Pravega client needs to be reinitialized", e);
                    close(reader);
                    reader = createReader(clientFactory);
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
            } while (isRunning);

            if (event.getEvent() == null)
                logger.log(Level.INFO, String.format("No more events from %s/%s: exiting", readerConfig.getScope(), readerConfig.getStream()));

            logger.log(Level.INFO, String.format("Read %d events in %d ms", eventCounter, System.currentTimeMillis() - executionStartTime));

            //if (eventCounter > 0) response.finishPartialResult(input);
            //else response.addEmptyResult(input, OperationStatus.SUCCESS, "OK", null);

            // make sure we close the reader before the client is closed, otherwise it seems the reader is not properly
            // removed from the reader group and may starve other readers in that group (i.e. in subsequent executions)
            close(reader);
        } catch (Throwable t) {
            close(reader);
            logger.log(Level.SEVERE, String.format("Error reading from %s/%s", readerConfig.getScope(), readerConfig.getStream()), t);
            //ResponseUtil.addExceptionFailure(response, input, t);
        }
    }

    @Override
    public void stop() {
        isRunning = false;

    }

    // Exception free
    private void close(EventStreamReader reader) {
        if (reader != null) try {
            reader.close();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Could not close Pravega reader", t);
        }
    }
}
