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
import java.util.concurrent.CompletableFuture;

import io.pravega.client.ClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.JavaSerializer;

/**
 * A simple example app that uses a Pravega Writer to write to a given scope and stream.
 */
public class HelloWorldWriter {

    public final String scope;
    public final String streamName;
    public final URI controllerURI;

    public HelloWorldWriter(String scope, String streamName, URI controllerURI) {
        this.scope = scope;
        this.streamName = streamName;
        this.controllerURI = controllerURI;
    }

    public void run(String routingKey, String message) {
        StreamManager streamManager = StreamManager.create(controllerURI);
        final boolean scopeIsNew = streamManager.createScope(scope);

        StreamConfiguration streamConfig = StreamConfiguration.builder()
                .scalingPolicy(ScalingPolicy.fixed(1))
                .build();
        final boolean streamIsNew = streamManager.createStream(scope, streamName, streamConfig);

        try (ClientFactory clientFactory = ClientFactory.withScope(scope, controllerURI);
             EventStreamWriter<String> writer = clientFactory.createEventWriter(streamName,
                                                                                 new JavaSerializer<String>(),
                                                                                 EventWriterConfig.builder().build())) {
            
            System.out.format("Writing message: '%s' with routing-key: '%s' to stream '%s / %s'%n",
                    message, routingKey, scope, streamName);
            final CompletableFuture writeFuture = writer.writeEvent(routingKey, message);
        }
    }

    public static void main(String[] args) {
        final String scope = Constants.DEFAULT_SCOPE;
        final String streamName = Constants.DEFAULT_STREAM_NAME;
        final String uriString = Constants.DEFAULT_CONTROLLER_URI;
        final URI controllerURI = URI.create(uriString);
        
        HelloWorldWriter hww = new HelloWorldWriter(scope, streamName, controllerURI);
        
        final String routingKey = Constants.DEFAULT_ROUTING_KEY;
        final String message = Constants.DEFAULT_MESSAGE;
        hww.run(routingKey, message);
    }

}