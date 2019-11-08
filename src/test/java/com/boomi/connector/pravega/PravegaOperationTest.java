package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationType;
import com.boomi.connector.testutil.ConnectorTester;
import com.boomi.connector.testutil.SimpleOperationResult;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.JavaSerializer;
import io.pravega.local.InProcPravegaCluster;
import io.pravega.local.LocalPravegaEmulator;
import io.pravega.local.SingleNodeConfig;
import io.pravega.segmentstore.server.store.ServiceBuilderConfig;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Dave Hock
 * @author Mohammad Omar Faruk
 * @author Stu Arnett
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PravegaOperationTest {
    private static final Logger log = LoggerFactory.getLogger(PravegaOperationTest.class);

    private static final String PRAVEGA_SCOPE = "boomi-test";
    private static final String PRAVEGA_STREAM = "connector-test";
    private static final String PRAVEGA_CONTROLLER_URI = "tcp://127.0.0.1:9090";

    private static final String FIXED_ROUTING_KEY = "test-1";
    private static final String JSON_ROUTING_KEY = "message";

    private static final long READ_TIMEOUT = 5000; // 5 seconds
    private static final String READER_GROUP = "connector-test";

    private static InProcPravegaCluster localPravega;
    private static String json;
    private static EventStreamClientFactory pravegaClientFactory;
    private static EventStreamWriter<String> pravegaWriter;
    private static EventStreamReader<String> pravegaReader;

    @BeforeAll
    public static void startPravegaStandalone() throws Exception {
        // start Pravega stand-alone
        Properties standaloneProperties = new Properties();
        standaloneProperties.load(PravegaOperationTest.class.getResourceAsStream("/standalone-config.properties"));
        ServiceBuilderConfig config = ServiceBuilderConfig
                .builder()
                .include(standaloneProperties)
                .include(System.getProperties())
                .build();
        SingleNodeConfig conf = config.getConfig(SingleNodeConfig::builder);

        localPravega = LocalPravegaEmulator.builder()
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

        // initialize Pravega client
        pravegaClientFactory = initClientFactory();

        // init writer
        pravegaWriter = pravegaClientFactory.createEventWriter(PRAVEGA_STREAM,
                new JavaSerializer<>(), EventWriterConfig.builder().build());

        // init reader
        pravegaReader = pravegaClientFactory.createReader(UUID.randomUUID().toString(), READER_GROUP,
                new JavaSerializer<>(), io.pravega.client.stream.ReaderConfig.builder().build());

        // initialize test event data
        // must use random generated data to avoid false positives from previous tests
        String randomMessage = UUID.randomUUID().toString();
        json = "{\"name\":\"foo\",\"message\":\"" + randomMessage + "\"}";
    }

    private static EventStreamClientFactory initClientFactory() {
        // configure client
        ClientConfig clientConfig = ClientConfig.builder().controllerURI(URI.create(PRAVEGA_CONTROLLER_URI)).build();

        // create stream manager
        StreamManager streamManager = StreamManager.create(clientConfig);

        // create scope
        streamManager.createScope(PRAVEGA_SCOPE);

        // configure stream
        StreamConfiguration.StreamConfigurationBuilder streamBuilder = StreamConfiguration.builder();
        streamBuilder.scalingPolicy(ScalingPolicy.byEventRate(20, 2, 1));

        // create stream
        streamManager.createStream(PRAVEGA_SCOPE, PRAVEGA_STREAM, streamBuilder.build());

        // create reader group
        ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(PRAVEGA_SCOPE, PRAVEGA_STREAM)).build();
        try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(PRAVEGA_SCOPE, clientConfig)) {
            readerGroupManager.createReaderGroup(READER_GROUP, readerGroupConfig);
        }

        // create client factory
        return EventStreamClientFactory.withScope(PRAVEGA_SCOPE, clientConfig);
    }

    @AfterAll
    public static void shutdownPravega() throws Exception {
        // shut down client
        pravegaClientFactory.close();

        // shut down Pravega stand-alone
        if (localPravega != null) localPravega.close();
    }

    @Test
    @Order(1)
    public void testCreateWithJsonRoutingKey() {
        // create tail reader that should only see one event
        try (PravegaConnector connector = new PravegaConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.NAME_PROPERTY, PRAVEGA_STREAM);

            Map<String, Object> opProps = new HashMap<>();
            opProps.put(Constants.ROUTINGKEY_NEEDED_PROPERTY, true);
            opProps.put(Constants.ROUTINGKEY_CONFIG_VALUE_PROPERTY, JSON_ROUTING_KEY);
            tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

            // prep test message
            List<InputStream> inputs = new ArrayList<>();
            inputs.add(new ByteArrayInputStream(json.getBytes()));

            // send the test message through the connector
            List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
            assertEquals("OK", actual.get(0).getStatusCode());

            // read from stream and verify event data
            EventRead<String> event = pravegaReader.readNextEvent(READ_TIMEOUT);

            // validate message data
            assertNotNull(event);
            assertEquals(json, event.getEvent());
        }
    }

    @Test
    @Order(2)
    public void testCreateOperation() {
        // create tail reader that should only see one event
        try (PravegaConnector connector = new PravegaConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.NAME_PROPERTY, PRAVEGA_STREAM);

            Map<String, Object> opProps = new HashMap<>();
            opProps.put(Constants.ROUTINGKEY_NEEDED_PROPERTY, false);
            opProps.put(Constants.FIXED_ROUTINGKEY_PROPERTY, FIXED_ROUTING_KEY);
            tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

            // prep test message
            List<InputStream> inputs = new ArrayList<>();
            inputs.add(new ByteArrayInputStream(json.getBytes()));

            // send the test message through the connector
            List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
            assertEquals("OK", actual.get(0).getStatusCode());

            // read from stream and verify event data
            EventRead<String> event = pravegaReader.readNextEvent(READ_TIMEOUT);

            // validate message data
            assertNotNull(event);
            assertEquals(json, event.getEvent());
        }
    }

    @Test
    @Order(3)
    public void testQueryOperation() throws Exception {
        try (PravegaConnector connector = new PravegaConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.NAME_PROPERTY, PRAVEGA_STREAM);

            Map<String, Object> opProps = new HashMap<>();
            opProps.put(Constants.READTIMEOUT_PROPERTY, 5000L);

            // write test event to stream first
            pravegaWriter.writeEvent(json).get();

            // execute the connector to read the event
            tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);
            List<SimpleOperationResult> results = tester.executeQueryOperation(null);
            assertEquals("OK", results.get(0).getStatusCode());

            // validate message data
            List payloads = results.get(0).getPayloads();
            System.out.println("Number of documents:" + payloads.size());
            // we should get one document
            assertEquals(1, payloads.size());
            byte[] documentBytes = (byte[]) payloads.get(0);
            // document should match our test message
            assertEquals(json, new String(documentBytes));
        }
    }
}
