package io.pravega.connector.boomi;

import com.boomi.connector.api.OperationContext;
import com.boomi.connector.api.PayloadUtil;
import com.boomi.connector.api.listen.Listener;
import com.boomi.connector.util.listen.UnmanagedListenOperation;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.TruncatedDataException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaListenOperation extends UnmanagedListenOperation {

    private static final Logger logger = Logger.getLogger(PravegaListenOperation.class.getName());

    private ReaderConfig readerConfig;
    private ClientConfig clientConfig;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    protected PravegaListenOperation(OperationContext context) {
        super(context);
        readerConfig = new ReaderConfig(context);
        clientConfig = PravegaUtil.createClientConfig(readerConfig);

        // create reader group
        PravegaUtil.createReaderGroup(readerConfig);
    }

    @Override
    protected void start(Listener listener) {
        isRunning.set(true);

        EventStreamReader<String> reader = null;
        try (EventStreamClientFactory clientFactory = PravegaUtil.createClientFactory(readerConfig, clientConfig)) {
            reader = PravegaUtil.createReader(readerConfig, clientFactory);

            logger.info(String.format("Reading events from %s/%s", readerConfig.getScope(), readerConfig.getStream()));
            do {
                try {
                    EventRead<String> event = reader.readNextEvent(readerConfig.getReadTimeout());
                    if (event.getEvent() != null) {
                        logger.log(Level.FINE, String.format("Listener Read event size: %d", event.getEvent().length()));
                        listener.submit(PayloadUtil.toPayload(event.getEvent()));
                    } // could check for watermark or checkpoint in else block
                } catch (ReinitializationRequiredException e) {
                    // There are certain circumstances where the reader needs to be reinitialized
                    logger.log(Level.WARNING, "Caught ReinitializationRequiredException - Pravega client needs to be reinitialized", e);
                    close(reader);
                    reader = PravegaUtil.createReader(readerConfig, clientFactory);
                } catch (TruncatedDataException e) {
                    // Assuming nothing needs to be done here and that the next call to readNextEvent() will return the next event
                    logger.log(Level.WARNING, "Caught TruncatedDataException", e);
                }
                // keep looping until we are stopped
            } while (isRunning.get());

            // make sure we close the reader before the client is closed, otherwise it seems the reader is not properly
            // removed from the reader group and may starve other readers in that group (i.e. in subsequent executions)
            close(reader);
        } catch (Throwable t) {
            close(reader);
            logger.log(Level.SEVERE, String.format("Error reading from %s/%s", readerConfig.getScope(), readerConfig.getStream()), t);
        }
    }

    @Override
    public void stop() {
        isRunning.set(false);
    }

    // Exception free
    private void close(EventStreamReader<?> reader) {
        if (reader != null) try {
            reader.close();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Could not close Pravega reader", t);
        }
    }
}
