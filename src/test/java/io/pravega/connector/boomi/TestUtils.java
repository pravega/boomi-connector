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
    private static final String PRAVEGA_ENDPOINT_KEY = "pravega_endpoint";
    private static final String AUTH_TYPE_KEY = "auth_type";

    static String PRAVEGA_CONTROLLER_URI = "";
    static String PRAVEGA_NAUT_CONTROLLER_URI = "";
    static String AUTH_TYPE = "";
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

    private TestUtils() {
    }

    public static void loadPropertiesFile() throws Exception {
        Properties props = TestConfig.getProperties();
        PRAVEGA_CONTROLLER_URI = TestConfig.getPropertyNotEmpty(props, PRAVEGA_ENDPOINT_KEY);
        PRAVEGA_NAUT_CONTROLLER_URI = TestConfig.getPropertyNotEmpty(props, SDP_ENDPOINT_KEY);
        AUTH_TYPE = TestConfig.getPropertyNotEmpty(props, AUTH_TYPE_KEY);
    }
}
