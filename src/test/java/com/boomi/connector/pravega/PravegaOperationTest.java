package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationType;
import com.boomi.connector.testutil.ConnectorTester;
import com.boomi.connector.testutil.SimpleOperationResult;
import io.pravega.local.InProcPravegaCluster;
import io.pravega.local.LocalPravegaEmulator;
import io.pravega.local.SingleNodeConfig;
import io.pravega.segmentstore.server.store.ServiceBuilderConfig;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dave Hock
 * @author Mohammad Omar Faruk
 * @author Stu Arnett
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PravegaOperationTest {
    private static final Logger log = LoggerFactory.getLogger(PravegaOperationTest.class);

    private static InProcPravegaCluster localPravega;
    private static String json;

    @BeforeAll
    public static void startPravegaStandalone() throws Exception {
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

        String randomMessage = UUID.randomUUID().toString();
        json = "{\"name\":\"foo\",\"message\":\"" + randomMessage + "\"}";
    }

    @AfterAll
    public static void shutdownPravega() throws Exception {
        if (localPravega != null) localPravega.close();
    }

    @Test
    @Order(1)
    public void testCreateOperation() {
        try (PravegaConnector connector = new PravegaConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.URI_PROPERTY, TestConstants.DEFAULT_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, TestConstants.DEFAULT_SCOPE);
            connProps.put(Constants.NAME_PROPERTY, TestConstants.DEFAULT_STREAM_NAME);

            Map<String, Object> opProps = new HashMap<>();
            opProps.put(Constants.ROUTINGKEY_NEEDED_PROPERTY, true);
            opProps.put(Constants.FIXED_ROUTINGKEY_PROPERTY, TestConstants.DEFAULT_ROUTING_KEY);
            opProps.put(Constants.ROUTINGKEY_CONFIG_VALUE_PROPERTY, TestConstants.DEFAULT_ROUTING_CONFIG_VALUE);
            tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

            // must use random generated test data to avoid false positive for existing events in the stream
            List<InputStream> inputs = new ArrayList<>();
            inputs.add(new ByteArrayInputStream(json.getBytes()));

            List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
            assertEquals("OK", actual.get(0).getStatusCode());

            // TODO: read from stream and verify event data, instead of relying on test execution order
        }
    }

    @Test
    @Order(2)
    public void testQueryOperation() {
        try (PravegaConnector connector = new PravegaConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.URI_PROPERTY, TestConstants.DEFAULT_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, TestConstants.DEFAULT_SCOPE);
            connProps.put(Constants.NAME_PROPERTY, TestConstants.DEFAULT_STREAM_NAME);

            Map<String, Object> opProps = new HashMap<>();
            opProps.put(Constants.READTIMEOUT_PROPERTY, 5000L);

            // TODO: send random data to stream first, instead of relying on test execution order

            tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);
            List<SimpleOperationResult> results = tester.executeQueryOperation(null);
            assertEquals("OK", results.get(0).getStatusCode());

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
