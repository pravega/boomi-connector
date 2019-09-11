package com.boomi;


import static com.boomi.constants.PravegaConstants.MESSAGE;
import static com.boomi.constants.PravegaConstants.CONTROLLER_URI;
import static com.boomi.constants.PravegaConstants.ROUTING_KEY;
import static com.boomi.constants.PravegaConstants.STREAM_NAME;
import static com.boomi.constants.PravegaConstants.STREAM_SCOPE;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.PropertyMap;
import com.boomi.connector.util.BaseConnection;

import io.pravega.client.ClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.JavaSerializer;

public class PravegaConnection extends BaseConnection {

	private Logger logger = Logger.getLogger(PravegaConnection.class.getName());

	private String host;	
	public String scope;
	public String streamName;
	private String routingKey;
	private String message;
	private URI controllerURI;

	public PravegaConnection(BrowseContext context) {
		super(context);
		PropertyMap propertyMap = context.getConnectionProperties();
		host = propertyMap.getProperty(CONTROLLER_URI);	
		streamName = propertyMap.getProperty(STREAM_NAME);
		scope = propertyMap.getProperty(STREAM_SCOPE);
		routingKey = propertyMap.getProperty(ROUTING_KEY);
		message = propertyMap.getProperty(MESSAGE);
		controllerURI = URI.create(host);
		run(routingKey, message);
	}

	public void run(String routingKey, String message) {
		
		StreamManager streamManager = StreamManager.create(controllerURI);
		
		logger.info("getting started..");
		final boolean scopeIsNew = streamManager.createScope(scope);
		
		logger.info("came inside scope");

		StreamConfiguration streamConfig = StreamConfiguration.builder().scalingPolicy(ScalingPolicy.fixed(1)).build();
		final boolean streamIsNew = streamManager.createStream(scope, streamName, streamConfig);

		try (ClientFactory clientFactory = ClientFactory.withScope(scope, controllerURI);
				EventStreamWriter<String> writer = clientFactory.createEventWriter(streamName,
						new JavaSerializer<String>(), EventWriterConfig.builder().build())) {

			System.out.format("Writing message: '%s' with routing-key: '%s' to stream '%s / %s'%n", message, routingKey,
					scope, streamName);
			final CompletableFuture writeFuture = writer.writeEvent(routingKey, message);
		}
	}
	
	

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public URI getControllerURI() {
		return controllerURI;
	}

	public void setControllerURI(URI controllerURI) {
		this.controllerURI = controllerURI;
	}

	

}