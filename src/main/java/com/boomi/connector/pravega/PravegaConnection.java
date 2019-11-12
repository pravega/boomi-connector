package com.boomi.connector.pravega;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.util.BaseConnection;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.DefaultCredentials;

import java.net.URI;

public class PravegaConnection extends BaseConnection implements AutoCloseable {
    private PravegaConfig pravegaConfig;
    private ClientConfig clientConfig;
    private EventStreamClientFactory clientFactory;

    public PravegaConnection(BrowseContext context) {
        super(context);
        pravegaConfig = PravegaConfig.fromContext(context);

        // configure client
        ClientConfig.ClientConfigBuilder clientBuilder = ClientConfig.builder().controllerURI(URI.create(pravegaConfig.getControllerUri().toString()));
        if (pravegaConfig.isEnableAuth())
            clientBuilder.credentials(new DefaultCredentials(pravegaConfig.getPassword(), pravegaConfig.getUserName()));
        clientConfig = clientBuilder.build();

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
        clientFactory = EventStreamClientFactory.withScope(pravegaConfig.getScope(), clientConfig);
    }

    @Override
    public void close() {
        if (clientFactory != null) clientFactory.close();
    }

    public PravegaConfig getPravegaConfig() {
        return pravegaConfig;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public EventStreamClientFactory getClientFactory() {
        return clientFactory;
    }
}
