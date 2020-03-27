package io.pravega.connector.boomi;

import com.boomi.connector.api.ConnectionTester;
import com.boomi.connector.api.ConnectorException;
import com.boomi.connector.api.OperationType;
import com.boomi.connector.testutil.ConnectorTester;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.NoSuchScopeException;
import io.pravega.local.InProcPravegaCluster;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class PravegaBrowserTest {
    private static InProcPravegaCluster localPravega;

    @BeforeAll
    public static void beforeAll() throws Exception {
        localPravega = TestUtils.startStandalone();
    }

    @AfterAll
    public static void afterAll() throws Exception {
        if (localPravega != null) localPravega.close();
    }

    @Test
    public void testTestConnectorBadPort() throws Exception {
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, "tcp://localhost:8123");
        connProps.put(Constants.SCOPE_PROPERTY, "foo");
        connProps.put(Constants.STREAM_PROPERTY, "bar");
        connProps.put(Constants.AUTH_TYPE_PROPERTY, Constants.AUTH_TYPE_PROPERTY_NONE);

        Map<String, Object> opProps = new HashMap<>();

        tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);

        ConnectionTester connTester = new PravegaBrowser(tester.getOperationContext());

        try {
            connTester.testConnection();
            Assertions.fail("connection test should have failed");
        } catch (ConnectorException e) {
            Assertions.assertFalse(e.getCause() instanceof NoSuchScopeException);
        }
    }

    @Test
    public void testTestConnectorBadScope() throws Exception {
        String scope = "foo234", stream = "bar";
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, "tcp://localhost:9090");
        connProps.put(Constants.AUTH_TYPE_PROPERTY, Constants.AUTH_TYPE_PROPERTY_NONE);
        connProps.put(Constants.SCOPE_PROPERTY, scope);
        connProps.put(Constants.STREAM_PROPERTY, stream);
        connProps.put(Constants.CREATE_SCOPE_PROPERTY, false);

        Map<String, Object> opProps = new HashMap<>();

        tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);

        ConnectionTester connTester = new PravegaBrowser(tester.getOperationContext());

        try {
            connTester.testConnection();
            Assertions.fail("connection test should have failed");
        } catch (ConnectorException e) {
            Assertions.assertTrue(e.getCause() instanceof NoSuchScopeException);
        }
    }

    @Test
    public void testTestConnectorSuccess() throws Exception {
        String scope = "foo", stream = "bar";
        PravegaConfig pravegaConfig = new PravegaConfig();
        pravegaConfig.setControllerUri(new URI(TestUtils.PRAVEGA_CONTROLLER_URI));
        pravegaConfig.setScope(scope);
        pravegaConfig.setStream(stream);
        pravegaConfig.setCreateScope(true);
        pravegaConfig.setAuth(Constants.AUTH_TYPE_PROPERTY_NONE);

        // this will create the scope
        EventStreamClientFactory clientFactory = PravegaUtil.createClientFactory(pravegaConfig);
        clientFactory.close();

        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.AUTH_TYPE_PROPERTY, Constants.AUTH_TYPE_PROPERTY_NONE);
        connProps.put(Constants.SCOPE_PROPERTY, scope);
        connProps.put(Constants.STREAM_PROPERTY, stream);
        connProps.put(Constants.CREATE_SCOPE_PROPERTY, true);

        Map<String, Object> opProps = new HashMap<>();

        tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);

        ConnectionTester connTester = new PravegaBrowser(tester.getOperationContext());

        try {
            connTester.testConnection();
        } catch (ConnectorException e) {
            Assertions.fail("connection test should have succeeded", e);
        }
    }

    @Test
    public void testTestNautilusConnector() throws Exception {
        String home = System.getProperty("user.home");
        String jsonData = new String(Files.readAllBytes(Paths.get(home + "/keycloak.json")));

        String scope = "boomi-test-project", stream = "test-stream-1";
        PravegaConfig pravegaConfig = new PravegaConfig();
        pravegaConfig.setControllerUri(new URI(TestUtils.PRAVEGA_NAUT_CONTROLLER_URI));
        pravegaConfig.setScope(scope);
        pravegaConfig.setStream(stream);
        pravegaConfig.setCreateScope(false);
        pravegaConfig.setAuth(Constants.AUTH_TYPE_PROPERTY_KEYCLOAK);
        pravegaConfig.setKeycloakJSONPath(home + "/keycloak.json");

        // this will create the scope
        EventStreamClientFactory clientFactory = PravegaUtil.createClientFactory(pravegaConfig);
        clientFactory.close();

        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_NAUT_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, scope);
        connProps.put(Constants.STREAM_PROPERTY, stream);
        connProps.put(Constants.CREATE_SCOPE_PROPERTY, false);
        connProps.put(Constants.AUTH_TYPE_PROPERTY, Constants.AUTH_TYPE_PROPERTY_KEYCLOAK);
        connProps.put(Constants.AUTH_PROPERTY_KEYCLOAK_JSON, jsonData);

        Map<String, Object> opProps = new HashMap<>();
        tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);
        ConnectionTester connTester = new PravegaBrowser(tester.getOperationContext());

        try {
            connTester.testConnection();
        } catch (ConnectorException e) {
            Assertions.fail("connection test should have succeeded", e);
        }

    }
}
