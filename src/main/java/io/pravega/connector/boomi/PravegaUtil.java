/*
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package io.pravega.connector.boomi;

import com.boomi.connector.api.ConnectorContext;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamInfo;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.DefaultCredentials;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import io.pravega.keycloak.client.PravegaKeycloakCredentials;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

final class PravegaUtil {
    private static final Logger logger = Logger.getLogger(PravegaUtil.class.getName());

    static ClientConfig createClientConfig(PravegaConfig pravegaConfig) {
        ClientConfig.ClientConfigBuilder clientBuilder = ClientConfig.builder().controllerURI(URI.create(pravegaConfig.getControllerUri().toString()));
        if (pravegaConfig.getAuth() == PravegaConfig.AuthenticationType.Basic)
            clientBuilder.credentials(new DefaultCredentials(pravegaConfig.getPassword(), pravegaConfig.getUserName()));
        else if (pravegaConfig.getAuth() == PravegaConfig.AuthenticationType.Keycloak)
            clientBuilder.credentials(new PravegaKeycloakCredentials(pravegaConfig.getKeycloakJSONString()));
        return clientBuilder.build();
    }

    static void createReaderGroup(ReaderConfig readerConfig) {
        Stream stream = Stream.of(readerConfig.getScope(), readerConfig.getStream());
        StreamCut startStreamCut = StreamCut.UNBOUNDED;
        if (readerConfig.getInitialReaderPosition() == ReaderConfig.InitialReaderPosition.Tail) {
            startStreamCut = getStreamInfo(readerConfig).getTailStreamCut();
        }
        Map<Stream, StreamCut> streamCuts = new HashMap<>();
        streamCuts.put(stream, startStreamCut);
        ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder().stream(stream).startFromStreamCuts(streamCuts).build();
        try (ReaderGroupManager readerGroupManager =
                     ReaderGroupManager.withScope(readerConfig.getScope(), createClientConfig(readerConfig))) {
            readerGroupManager.createReaderGroup(readerConfig.getReaderGroup(), readerGroupConfig);
        }
    }

    public static StreamInfo getStreamInfo(ReaderConfig readerConfig) {
        ClientConfig clientConfig = createClientConfig(readerConfig);
        try (StreamManager streamManager = StreamManager.create(clientConfig)) {
            return streamManager.getStreamInfo(readerConfig.getScope(), readerConfig.getStream());
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

    static void testConnection(ConnectorContext connectorContext, String keycloakJSONString) {
        PravegaConfig pravegaConfig = new PravegaConfig(connectorContext, keycloakJSONString);

        // create stream manager
        try (StreamManager streamManager = StreamManager.create(createClientConfig(pravegaConfig))) {
            // check connection and scope
            try {
                streamManager.listStreams(pravegaConfig.getScope()).hasNext();
            } catch (Exception e) {
                // peel exception from Future
                if (e instanceof CompletionException) e = (Exception) e.getCause();
                // does the scope not exist?
                if (e instanceof NoSuchScopeException) {
                    // scope doesn't exist, and we can't create it
                    if (!pravegaConfig.isCreateScope()) throw (NoSuchScopeException) e;
                    // scope doesn't exist, but we are supposed to create it - this is ok
                } else {
                    // some other problem
                    if (e instanceof RuntimeException) throw (RuntimeException) e;
                    else throw new RuntimeException(e);
                }
            }
        }
    }

    // caller must close
    static EventStreamReader<String> createReader(ReaderConfig readerConfig, EventStreamClientFactory clientFactory) {
        String readerId = UUID.randomUUID().toString();
        logger.info(String.format("Creating reader for stream %s / %s using group %s and ID %s",
                readerConfig.getScope(), readerConfig.getStream(), readerConfig.getReaderGroup(), readerId));
        return clientFactory.createReader(readerId, readerConfig.getReaderGroup(),
                new UTF8StringSerializer(), io.pravega.client.stream.ReaderConfig.builder().build());
    }

    static String getKeycloakCredentialsString(ConnectorContext context) {
        Map<String, Object> props = context.getConnectionProperties();
        String auth = (String) props.get(Constants.AUTH_TYPE_PROPERTY);
        PravegaConfig.AuthenticationType authType = PravegaConfig.AuthenticationType.valueOf(auth);
        if (authType == PravegaConfig.AuthenticationType.Keycloak) {
            String keycloakJSONString = (String) props.get(Constants.AUTH_PROPERTY_KEYCLOAK_JSON);
            return keycloakJSONString;
        }
        return null;
    }

    private PravegaUtil() {
    }
}
