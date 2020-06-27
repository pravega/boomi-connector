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

import io.pravega.client.ClientConfig;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.local.InProcPravegaCluster;
import io.pravega.local.LocalPravegaEmulator;
import io.pravega.local.SingleNodeConfig;
import io.pravega.segmentstore.server.store.ServiceBuilderConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

final class TestUtils {
    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);

    private static final String SDP_ENDPOINT_KEY = "sdp_endpoint";
    private static final String SDP_AUTH_TYPE_KEY = "sdp_auth_type";
    private static final String LOCAL_PRAVEGA_AUTH_TYPE_KEY = "local_pravega_auth_type";
    private static final String INTERVAL_KEY = "interval";
    private static final String INTERVAL_UNIT_KEY = "interval_unit";

    static String PRAVEGA_CONTROLLER_URI = "tcp://127.0.0.1:9090";
    static String PRAVEGA_NAUT_CONTROLLER_URI = "";
    static String SDP_AUTH_TYPE = "";
    static String LOCAL_PRAVEGA_AUTH_TYPE = "";
    static long INTERVAL;
    static String INTERVAL_UNIT = "";

    // Caller must close
    static InProcPravegaCluster startStandalone() throws Exception {
        // start Pravega stand-alone
        Properties standaloneProperties = new Properties();
        standaloneProperties.load(PravegaOperationTest.class.getResourceAsStream("/standalone-config.properties"));
        ServiceBuilderConfig config = ServiceBuilderConfig
                .builder()
                .include(standaloneProperties)
                .include(System.getProperties())
                .build();
        SingleNodeConfig conf = config.getConfig(SingleNodeConfig::builder);

        InProcPravegaCluster localPravega = LocalPravegaEmulator.builder()
                .controllerPort(conf.getControllerPort())
                .segmentStorePort(conf.getSegmentStorePort())
                .zkPort(conf.getZkPort())
                .restServerPort(conf.getRestServerPort())
                .enableRestServer(conf.isEnableRestServer())
                .enableAuth(conf.isEnableAuth())
                .enableTls(conf.isEnableTls())
                .certFile(conf.getCertFile())
                .keyFile(conf.getKeyFile())
                .enableTlsReload(conf.isEnableSegmentStoreTlsReload())
                .jksKeyFile(conf.getKeyStoreJKS())
                .jksTrustFile(conf.getTrustStoreJKS())
                .keyPasswordFile(conf.getKeyStoreJKSPasswordFile())
                .passwdFile(conf.getPasswdFile())
                .userName(conf.getUserName())
                .passwd(conf.getPasswd())
                .build()
                .getInProcPravegaCluster();

        log.warn("Starting Pravega Emulator with ports: ZK port {}, controllerPort {}, SegmentStorePort {}",
                conf.getZkPort(), conf.getControllerPort(), conf.getSegmentStorePort());

        localPravega.start();

        log.warn("Pravega Sandbox is running locally now. You could access it at {}:{}",
                "127.0.0.1", conf.getControllerPort());

        return localPravega;
    }

    static void createStreams(ClientConfig clientConfig, String scope, String... streams) {
        // create stream manager
        try (StreamManager streamManager = StreamManager.create(clientConfig)) {

            // create scope
            streamManager.createScope(scope);

            // configure stream
            StreamConfiguration.StreamConfigurationBuilder streamBuilder = StreamConfiguration.builder();
            streamBuilder.scalingPolicy(ScalingPolicy.byEventRate(20, 2, 1));

            for (String stream : streams) {
                streamManager.createStream(scope, stream, streamBuilder.build());
            }
        }
    }

    static String generate9MBmessage() {
        char[] chars = new char[9000000];
        Arrays.fill(chars, 'a');
        String randomMessage = new String(chars);
        return "{\"name\":\"foo\",\"message\":\"" + randomMessage + "\"}";
    }

    static String generate2MBmessage() {
        char[] chars = new char[2000000];
        Arrays.fill(chars, 'a');
        String randomMessage = new String(chars);
        return "{\"name\":\"foo\",\"message\":\"" + randomMessage + "\"}";
    }

    static String generateJsonMessage() {
        // initialize test event data
        // must use random generated data to avoid false positives from previous tests
        String randomMessage = UUID.randomUUID().toString();
        return "{\"name\":\"foo\",\"message\":\"" + randomMessage + "\"}";
    }

    static String generateJsonMessage(int i) {
        // initialize test event data
        // must use random generated data to avoid false positives from previous tests
        String randomMessage = UUID.randomUUID().toString() + i;
        return "{\"name\":\"foo\",\"message\":\"" + randomMessage + "\"}";
    }

    private TestUtils() {
    }

    public static void loadPropertiesFile() throws Exception {
        Properties props = TestConfig.getProperties();
        PRAVEGA_NAUT_CONTROLLER_URI = TestConfig.getPropertyNotEmpty(props, SDP_ENDPOINT_KEY);
        SDP_AUTH_TYPE = TestConfig.getPropertyNotEmpty(props, SDP_AUTH_TYPE_KEY);
        LOCAL_PRAVEGA_AUTH_TYPE = TestConfig.getPropertyNotEmpty(props, LOCAL_PRAVEGA_AUTH_TYPE_KEY);
        INTERVAL = Long.parseLong(TestConfig.getPropertyNotEmpty(props, INTERVAL_KEY));
        INTERVAL_UNIT = TestConfig.getPropertyNotEmpty(props, INTERVAL_UNIT_KEY);
    }
}
