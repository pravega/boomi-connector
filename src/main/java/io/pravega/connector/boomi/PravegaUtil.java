package io.pravega.connector.boomi;

import com.boomi.connector.api.BrowseContext;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.DefaultCredentials;

import java.net.URI;
import java.util.concurrent.CompletionException;

final class PravegaUtil {
    static ClientConfig createClientConfig(PravegaConfig pravegaConfig) {
        ClientConfig.ClientConfigBuilder clientBuilder = ClientConfig.builder().controllerURI(URI.create(pravegaConfig.getControllerUri().toString()));
        if (pravegaConfig.isEnableAuth())
            clientBuilder.credentials(new DefaultCredentials(pravegaConfig.getPassword(), pravegaConfig.getUserName()));
        return clientBuilder.build();
    }

    static void createReaderGroup(ReaderConfig readerConfig) {
        ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(readerConfig.getScope(), readerConfig.getStream())).build();
        try (ReaderGroupManager readerGroupManager =
                     ReaderGroupManager.withScope(readerConfig.getScope(), createClientConfig(readerConfig))) {
            readerGroupManager.createReaderGroup(readerConfig.getReaderGroup(), readerGroupConfig);
        }
    }

    // Caller must close
    static EventStreamClientFactory createClientFactory(PravegaConfig pravegaConfig) {
        ClientConfig clientConfig = createClientConfig(pravegaConfig);

        // create stream manager
        try (StreamManager streamManager = StreamManager.create(clientConfig)) {
            // create scope
            if (pravegaConfig.isCreateScope()) streamManager.createScope(pravegaConfig.getScope());

            // configure stream
            StreamConfiguration.StreamConfigurationBuilder streamBuilder = StreamConfiguration.builder();
            streamBuilder.scalingPolicy(ScalingPolicy.byEventRate(20, 2, 1));

            //  create stream
            streamManager.createStream(pravegaConfig.getScope(), pravegaConfig.getStream(), streamBuilder.build());
        }

        // create client factory
        return EventStreamClientFactory.withScope(pravegaConfig.getScope(), clientConfig);
    }

    static void testConnection(BrowseContext browseContext) {
        PravegaConfig pravegaConfig = new PravegaConfig(browseContext);

        // create stream manager
        try (StreamManager streamManager = StreamManager.create(createClientConfig(pravegaConfig))) {
            // check connection and scope
            try {
                streamManager.listStreams(pravegaConfig.getScope()).hasNext();
            } catch (Throwable t) {
                // peel exception from Future
                if (t instanceof CompletionException) t = t.getCause();

                // does the scope not exist?
                if (t instanceof NoSuchScopeException) {
                    // scope doesn't exist, and we can't create it
                    if (!pravegaConfig.isCreateScope()) throw (NoSuchScopeException) t;

                    // scope doesn't exist, but we are supposed to create it - this is ok

                } else {
                    // some other problem
                    if (t instanceof RuntimeException) throw (RuntimeException) t;
                    else throw new RuntimeException(t);
                }
            }
        }
    }

    private PravegaUtil() {
    }
}
