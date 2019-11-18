package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationType;
import com.boomi.connector.testutil.ConnectorTester;
import com.boomi.connector.testutil.SimpleOperationResult;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import io.pravega.local.InProcPravegaCluster;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dave Hock
 * @author Mohammad Omar Faruk
 * @author Stu Arnett
 */
public class PravegaOperationTest {
    private static final Logger log = LoggerFactory.getLogger(PravegaOperationTest.class);

    private static final String PRAVEGA_SCOPE = "boomi-test";
    private static final String CREATE_OPERATION_STREAM = "connector-test-create";
    private static final String QUERY_OPERATION_STREAM = "connector-test-query";
    static final String PRAVEGA_CONTROLLER_URI = "tcp://127.0.0.1:9090";

    private static final String FIXED_ROUTING_KEY = "test-1";
    private static final String JSON_ROUTING_KEY = "message";

    private static final long READ_TIMEOUT = 2000; // 2 seconds
    private static final String CREATE_OPERATION_READER_GROUP = "connector-test-create-reader";
    private static final String QUERY_OPERATION_READER_GROUP = "connector-test-query-reader";

    private static ClientConfig clientConfig = ClientConfig.builder().controllerURI(URI.create(PRAVEGA_CONTROLLER_URI)).build();
    private static InProcPravegaCluster localPravega;
    private static EventStreamClientFactory pravegaClientFactory;
    private static EventStreamWriter<String> pravegaQueryOperationWriter;
    private static EventStreamReader<String> pravegaCreateOperationReader;

    @BeforeAll
    public static void classSetup() throws Exception {
        localPravega = PravegaHelper.startStandalone();

        // initialize Pravega client
        pravegaClientFactory = initClient();

        // init Query operation test writer
        pravegaQueryOperationWriter = pravegaClientFactory.createEventWriter(QUERY_OPERATION_STREAM,
                new UTF8StringSerializer(), EventWriterConfig.builder().build());

        // init Create operation test reader
        pravegaCreateOperationReader = pravegaClientFactory.createReader(UUID.randomUUID().toString(), CREATE_OPERATION_READER_GROUP,
                new UTF8StringSerializer(), io.pravega.client.stream.ReaderConfig.builder().build());
    }

    private static EventStreamClientFactory initClient() {
        // NOTE: in order to have consistent positions between readers and writers, we use separate streams for testing
        // Create and Query operations
        createStreams(CREATE_OPERATION_STREAM, QUERY_OPERATION_STREAM);

        // create Create operation test reader group
        ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .groupRefreshTimeMillis(0).stream(Stream.of(PRAVEGA_SCOPE, CREATE_OPERATION_STREAM)).build();
        try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(PRAVEGA_SCOPE, clientConfig)) {
            readerGroupManager.createReaderGroup(CREATE_OPERATION_READER_GROUP, readerGroupConfig);
        }

        // create client factory
        return EventStreamClientFactory.withScope(PRAVEGA_SCOPE, clientConfig);
    }

    private static void createStreams(String... streams) {
        // create stream manager
        try (StreamManager streamManager = StreamManager.create(clientConfig)) {

            // create scope
            streamManager.createScope(PRAVEGA_SCOPE);

            // configure stream
            StreamConfiguration.StreamConfigurationBuilder streamBuilder = StreamConfiguration.builder();
            streamBuilder.scalingPolicy(ScalingPolicy.byEventRate(20, 2, 1));

            for (String stream : streams) {
                streamManager.createStream(PRAVEGA_SCOPE, stream, streamBuilder.build());
            }
        }
    }

    @AfterAll
    public static void classTearDown() throws Exception {
        // shut down client
        if (pravegaCreateOperationReader != null) pravegaCreateOperationReader.close();
        if (pravegaQueryOperationWriter != null) pravegaQueryOperationWriter.close();
        if (pravegaClientFactory != null) pravegaClientFactory.close();

        // shut down Pravega stand-alone
        if (localPravega != null) localPravega.close();
    }

    @Test
    public void testCreateWithJsonRoutingKey() throws Exception {
        String json = generateJsonMessage();
        try (PravegaTestConnector connector = new PravegaTestConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.CONTROLLER_URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.STREAM_PROPERTY, CREATE_OPERATION_STREAM);

            Map<String, Object> opProps = new HashMap<>();
            opProps.put(Constants.ROUTING_KEY_TYPE_PROPERTY, WriterConfig.RoutingKeyType.JsonReference.toString());
            opProps.put(Constants.ROUTING_KEY_PROPERTY, JSON_ROUTING_KEY);
            tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

            // prep test message
            List<InputStream> inputs = new ArrayList<>();
            inputs.add(new ByteArrayInputStream(json.getBytes()));

            // send the test message through the connector
            List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
            assertEquals("OK", actual.get(0).getStatusCode());

            // read from stream and verify event data
            EventRead<String> event;
            do {
                event = pravegaCreateOperationReader.readNextEvent(READ_TIMEOUT);
            } while (event.isCheckpoint());

            // validate message data
            assertNotNull(event.getEvent());
            assertEquals(json, event.getEvent());
        }
    }

    @Test
    public void testCreateOperation() throws Exception {
        String json = generateJsonMessage();
        try (PravegaTestConnector connector = new PravegaTestConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.CONTROLLER_URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.STREAM_PROPERTY, CREATE_OPERATION_STREAM);

            Map<String, Object> opProps = new HashMap<>();
            opProps.put(Constants.ROUTING_KEY_TYPE_PROPERTY, WriterConfig.RoutingKeyType.Fixed.toString());
            opProps.put(Constants.ROUTING_KEY_PROPERTY, FIXED_ROUTING_KEY);
            tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

            // prep test message
            List<InputStream> inputs = new ArrayList<>();
            inputs.add(new ByteArrayInputStream(json.getBytes()));

            // send the test message through the connector
            List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
            assertEquals("OK", actual.get(0).getStatusCode());

            // read from stream and verify event data
            EventRead<String> event;
            do {
                event = pravegaCreateOperationReader.readNextEvent(READ_TIMEOUT);
            } while (event.isCheckpoint());

            // validate message data
            assertNotNull(event.getEvent());
            assertEquals(json, event.getEvent());
        }
    }

    @Test
    public void testCreateWithNullRoutingKeyType() throws Exception {
        String json = generateJsonMessage();
        try (PravegaTestConnector connector = new PravegaTestConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.CONTROLLER_URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.STREAM_PROPERTY, CREATE_OPERATION_STREAM);

            Map<String, Object> opProps = new HashMap<>();
            tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

            // prep test message
            List<InputStream> inputs = new ArrayList<>();
            inputs.add(new ByteArrayInputStream(json.getBytes()));

            // send the test message through the connector
            List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
            assertEquals("OK", actual.get(0).getStatusCode());

            // read from stream and verify event data
            EventRead<String> event;
            do {
                event = pravegaCreateOperationReader.readNextEvent(READ_TIMEOUT);
            } while (event.isCheckpoint());

            // validate message data
            assertNotNull(event.getEvent());
            assertEquals(json, event.getEvent());
        }
    }

    @Test
    public void testQueryOperation() throws Exception {
        String json = generateJsonMessage();
        try (PravegaTestConnector connector = new PravegaTestConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.CONTROLLER_URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.STREAM_PROPERTY, QUERY_OPERATION_STREAM);

            Map<String, Object> opProps = new HashMap<>();
            opProps.put(Constants.READER_GROUP_PROPERTY, QUERY_OPERATION_READER_GROUP);
            opProps.put(Constants.READ_TIMEOUT_PROPERTY, 5000L);

            // write test event to stream first
            pravegaQueryOperationWriter.writeEvent(json).get();

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

    @Test
    public void testMaxReadPerExecution() throws Exception {
        String stream = "connector-test-max-read-time";
        long maxReadTime = 4L; // seconds
        long writeTime = maxReadTime * 10; // background writer is much slower than our reader here

        // create new test stream for this test case
        createStreams(stream);

        // use background writer (write for writeTime seconds)
        try (BackgroundStreamWriter backgroundWriter = new BackgroundStreamWriter(stream)) {
            Thread.sleep(writeTime * 1000);
            backgroundWriter.stop();
            backgroundWriter.waitForAck();
        }

        try (PravegaTestConnector connector = new PravegaTestConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.CONTROLLER_URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.STREAM_PROPERTY, stream);

            Map<String, Object> opProps = new HashMap<>();
            // no reader group specified, so Query operation will generate a unique reader group
            opProps.put(Constants.READ_TIMEOUT_PROPERTY, ReaderConfig.DEFAULT_READ_TIMEOUT);
            // should read for only maxReadTime seconds
            opProps.put(Constants.MAX_READ_TIME_PER_EXECUTION_PROPERTY, maxReadTime);

            // execute the connector to read events
            long startTime = System.currentTimeMillis();
            tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);
            List<SimpleOperationResult> results = tester.executeQueryOperation(null);
            long stopTime = System.currentTimeMillis();

            // validate read success
            for (SimpleOperationResult result : results) {
                assertEquals("OK", result.getStatusCode());
            }

            // we should have received multiple events
            assertTrue(results.size() > 0);
            assertTrue(results.get(0).getPayloads().size() > 1);
            // execution should have taken at least the max-read-time
            assertTrue(stopTime - startTime > maxReadTime * 1000);
            // execution should not have taken much long than that (add buffer for initialization and latency)
            assertTrue(stopTime - startTime < maxReadTime * 1000 + ReaderConfig.DEFAULT_READ_TIMEOUT + 2000);
        }
    }

    @Test
    public void testMaxEventsPerExecution() throws Exception {
        String stream = "connector-test-max-events";
        int maxEvents = 50000;
        int writeEvents = maxEvents * 2;

        // create new test stream for this test case
        createStreams(stream);

        // use background writer (write writeEvents events)
        try (BackgroundStreamWriter backgroundWriter = new BackgroundStreamWriter(stream, writeEvents)) {
            backgroundWriter.waitForMaxEvents();
        }

        try (PravegaTestConnector connector = new PravegaTestConnector()) {
            ConnectorTester tester = new ConnectorTester(connector);

            Map<String, Object> connProps = new HashMap<>();
            connProps.put(Constants.CONTROLLER_URI_PROPERTY, PRAVEGA_CONTROLLER_URI);
            connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
            connProps.put(Constants.STREAM_PROPERTY, stream);

            Map<String, Object> opProps = new HashMap<>();
            // no reader group specified, so Query operation will generate a unique reader group
            opProps.put(Constants.READ_TIMEOUT_PROPERTY, ReaderConfig.DEFAULT_READ_TIMEOUT);
            // should read only maxEvents events
            opProps.put(Constants.MAX_EVENTS_PER_EXECUTION_PROPERTY, (long) maxEvents);

            // execute the connector to read events
            tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);
            List<SimpleOperationResult> results = tester.executeQueryOperation(null);

            // should have exactly maxEvents
            assertTrue(results.size() > 0);
            assertEquals(maxEvents, results.get(0).getPayloads().size());
            // validate read success
            for (SimpleOperationResult result : results) {
                assertEquals("OK", result.getStatusCode());
            }
        }
    }

    private static String generateJsonMessage() {
        // initialize test event data
        // must use random generated data to avoid false positives from previous tests
        String randomMessage = UUID.randomUUID().toString();
        return "{\"name\":\"foo\",\"message\":\"" + randomMessage + "\"}";
    }

    private static class BackgroundStreamWriter implements Runnable, AutoCloseable {
        EventStreamWriter<String> eventWriter;
        AtomicBoolean running = new AtomicBoolean(true);
        int eventCounter = 0;
        int maxEvents;
        int errorCount = 0;
        int maxErrors = 10;
        long startTime;
        List<Future> futures = new ArrayList<>(500000); // don't let resizing slow us down
        String jsonMessage = generateJsonMessage();

        BackgroundStreamWriter(String stream) {
            this(stream, Integer.MAX_VALUE);
        }

        BackgroundStreamWriter(String stream, int maxEvents) {
            this.maxEvents = maxEvents;
            eventWriter = pravegaClientFactory.createEventWriter(stream, new UTF8StringSerializer(), EventWriterConfig.builder().build());
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

        @Override
        public synchronized void run() {
            startTime = System.currentTimeMillis();
            while (running.get() && eventCounter < maxEvents && errorCount < maxErrors) {
                try {
                    futures.add(eventWriter.writeEvent(jsonMessage));
                    eventCounter++;
                } catch (Exception e) {
                    log.error("error writing to stream from background writer", e);
                    errorCount++;
                }
            }
            running.set(false);
        }

        void stop() {
            running.set(false);
        }

        @Override
        public void close() {
            stop();
            synchronized (this) { // since run() is synchronized, this will wait until execution is complete
                if (eventWriter != null) {
                    eventWriter.close();
                    eventWriter = null;
                    log.info("wrote {} events in {} ms, with {} failures",
                            eventCounter, System.currentTimeMillis() - startTime, errorCount);
                }
            }
        }

        void waitForAck() throws Exception {
            for (Future future : futures) future.get();
        }

        void waitForMaxEvents() throws Exception {
            assert maxEvents < Integer.MAX_VALUE; // dummy detector

            while (running.get() && startTime == 0) { // wait until execution actually starts
                Thread.sleep(1000);
            }

            synchronized (this) { // since run() is synchronized, this will wait until execution is complete
                waitForAck();
            }
        }
    }
}
