/*
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 */
package com.boomi.connector.pravega;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import io.pravega.client.stream.Stream;

import io.pravega.client.ClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.ReaderGroupConfig;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.JavaSerializer;

/**
 * A simple example app that uses a Pravega Reader to read from a given scope and stream.
 */
public class HelloWorldReader {
    private static final int READER_TIMEOUT_MS = 2000;

    public final String scope;
    public final String streamName;
    public final URI controllerURI;

    public HelloWorldReader(String scope, String streamName, URI controllerURI) {
        this.scope = scope;
        this.streamName = streamName;
        this.controllerURI = controllerURI;
    }

    public void run() {
        StreamManager streamManager = StreamManager.create(controllerURI);
        
        final boolean scopeIsNew = streamManager.createScope(scope);
        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .scalingPolicy(ScalingPolicy.fixed(1))
                .build();
        final boolean streamIsNew = streamManager.createStream(scope, streamName, streamConfig);

        final String readerGroup = UUID.randomUUID().toString().replace("-", "");
        final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(scope, streamName))
                .build();
        try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(scope, controllerURI)) {
            readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);
        }

        try (ClientFactory clientFactory = ClientFactory.withScope(scope, controllerURI);
             EventStreamReader<String> reader = clientFactory.createReader("reader",
                                                                           readerGroup,
                                                                           new JavaSerializer<String>(),
                                                                           ReaderConfig.builder().build())) {
            System.out.format("Reading all the events from %s/%s%n", scope, streamName);
            EventRead<String> event = null;
            do {
                try {
                    event = reader.readNextEvent(READER_TIMEOUT_MS);
                    if (event.getEvent() != null) {
                        System.out.format("Read event '%s'%n", event.getEvent());
                    }
                } catch (ReinitializationRequiredException e) {
                    //There are certain circumstances where the reader needs to be reinitialized
                    e.printStackTrace();
                }
            } while (event.getEvent() != null);
            System.out.format("No more events from %s/%s%n", scope, streamName);
        }
    }

    public static void main(String[] args) {        
        final String scope = Constants.DEFAULT_SCOPE;
        final String streamName = Constants.DEFAULT_STREAM_NAME;
        final String uriString = Constants.DEFAULT_CONTROLLER_URI;
        final URI controllerURI = URI.create(uriString);
        
        HelloWorldReader hwr = new HelloWorldReader(scope, streamName, controllerURI);
        hwr.run();
    }

}