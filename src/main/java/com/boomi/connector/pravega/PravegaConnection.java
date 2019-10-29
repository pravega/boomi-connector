package com.boomi.connector.pravega;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.util.BaseConnection;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.ReaderGroupConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.Stream;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.DefaultCredentials;

import java.net.URI;

public class PravegaConnection extends BaseConnection implements AutoCloseable {
	private PravegaConfig pravegaConfig;
	private EventStreamClientFactory clientFactory;
	private String readerGroup;

	public PravegaConnection(BrowseContext context) {
		super(context);
		pravegaConfig = PravegaConfig.fromContext(context);

		// configure client
		ClientConfig.ClientConfigBuilder clientBuilder = ClientConfig.builder().controllerURI(URI.create(pravegaConfig.getControllerUri().toString()));
		if (pravegaConfig.isEnableAuth())
			clientBuilder.credentials(new DefaultCredentials(pravegaConfig.getPassword(), pravegaConfig.getUserName()));
		ClientConfig clientConfig = clientBuilder.build();

		// create stream manager
		StreamManager streamManager = StreamManager.create(clientConfig);

		// create scope
		if (pravegaConfig.isPravegaStandalone()) streamManager.createScope(pravegaConfig.getScope());

		// configure stream
		StreamConfiguration.StreamConfigurationBuilder streamBuilder = StreamConfiguration.builder();
		streamBuilder.scalingPolicy(ScalingPolicy.byEventRate(20, 2, 1));

		//  create stream
		streamManager.createStream(pravegaConfig.getScope(), pravegaConfig.getStreamName(), streamBuilder.build());

		// create client factory
		clientFactory = EventStreamClientFactory.withScope(pravegaConfig.getScope(), clientConfig);

		// create reader group
		// NOTE: this is currently unique per connector instance, but multiple Get operators will end up distributing
		// reads.. TODO: should this be unique per operation?
		readerGroup = "boomi-reader-" + pravegaConfig.getScope() + "-" + pravegaConfig.getStreamName()
				+ "-" + pravegaConfig.hashCode();
		final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
				.stream(Stream.of(pravegaConfig.getScope(), pravegaConfig.getStreamName())).build();

		ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(pravegaConfig.getScope(), clientConfig);
		readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);
	}

	@Override
	public void close() {
		if (clientFactory != null) clientFactory.close();
	}

	public PravegaConfig getPravegaConfig() {
		return pravegaConfig;
	}

	public EventStreamClientFactory getClientFactory() {
		return clientFactory;
	}

	public String getReaderGroup() {
		return readerGroup;
	}
}
