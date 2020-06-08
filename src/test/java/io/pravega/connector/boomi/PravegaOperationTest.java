package io.pravega.connector.boomi;

import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.api.OperationType;
import com.boomi.connector.api.Payload;
import com.boomi.connector.api.PayloadMetadata;
import com.boomi.connector.api.listen.*;
import com.boomi.connector.testutil.ConnectorTester;
import com.boomi.connector.testutil.SimpleOperationResult;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.stream.*;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import io.pravega.local.InProcPravegaCluster;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dave Hock
 * @author Mohammad Omar Faruk
 * @author Stu Arnett
 */
public class PravegaOperationTest {
    private static final Logger logger = Logger.getLogger(PravegaOperationTest.class.getName());
    private static final String PRAVEGA_SCOPE = "boomi-test";
    private static final String CREATE_OPERATION_STREAM = "connector-test-create";
    private static final String QUERY_OPERATION_STREAM = "connector-test-query";
    private static final String POLLING_LISTENER_STREAM = "connector-test-poll";

    private static final String FIXED_ROUTING_KEY = "test-1";
    private static final String JSON_ROUTING_KEY = "message";

    private static final long READ_TIMEOUT = 2000; // 2 seconds
    private static final long INTERVAL = 10;
    private static final String UNIT = "SECONDS";
    private static final String CREATE_OPERATION_READER_GROUP = "connector-test-create-reader";
    private static final String QUERY_OPERATION_READER_GROUP = "connector-test-query-reader";
    private static final String POLLING_OPERATION_READER_GROUP = "connector-test-polling-reader";

    private static ClientConfig clientConfig = ClientConfig.builder().controllerURI(URI.create(TestUtils.PRAVEGA_CONTROLLER_URI)).build();
    private static InProcPravegaCluster localPravega;
    private static EventStreamClientFactory pravegaClientFactory;
    private static EventStreamWriter<String> pravegaReadOperationWriter;
    private static EventStreamReader<String> pravegaWriteOperationReader;
    private static EventStreamWriter<String> pravegaPollingListenOperationWriter;

    @BeforeAll
    public static void classSetup() throws Exception {
        localPravega = TestUtils.startStandalone();

        // initialize Pravega client
        pravegaClientFactory = initClient();

        // init Query operation test writer
        pravegaReadOperationWriter = pravegaClientFactory.createEventWriter(QUERY_OPERATION_STREAM,
                new UTF8StringSerializer(), EventWriterConfig.builder().build());
        pravegaPollingListenOperationWriter = pravegaClientFactory.createEventWriter(POLLING_LISTENER_STREAM,
                new UTF8StringSerializer(), EventWriterConfig.builder().build());

        // init Create operation test reader
        pravegaWriteOperationReader = pravegaClientFactory.createReader(UUID.randomUUID().toString(), CREATE_OPERATION_READER_GROUP,
                new UTF8StringSerializer(), io.pravega.client.stream.ReaderConfig.builder().build());

    }

    private static EventStreamClientFactory initClient() {
        // NOTE: in order to have consistent positions between readers and writers, we use separate streams for testing
        // Create and Query operations
        TestUtils.createStreams(clientConfig, PRAVEGA_SCOPE, CREATE_OPERATION_STREAM, QUERY_OPERATION_STREAM, POLLING_LISTENER_STREAM);

        // create Create operation test reader group
        ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(PRAVEGA_SCOPE, CREATE_OPERATION_STREAM)).build();
        try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(PRAVEGA_SCOPE, clientConfig)) {
            readerGroupManager.createReaderGroup(CREATE_OPERATION_READER_GROUP, readerGroupConfig);
        }

        // create client factory
        return EventStreamClientFactory.withScope(PRAVEGA_SCOPE, clientConfig);
    }

    @AfterAll
    public static void classTearDown() throws Exception {
        // shut down client
        if (pravegaWriteOperationReader != null) pravegaWriteOperationReader.close();
        if (pravegaReadOperationWriter != null) pravegaReadOperationWriter.close();
        if (pravegaClientFactory != null) pravegaClientFactory.close();

        // shut down Pravega stand-alone
        if (localPravega != null) localPravega.close();
    }

    @Test
    public void testWriteWithJsonRoutingKey() {
        String json = TestUtils.generateJsonMessage();
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, CREATE_OPERATION_STREAM);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

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
            event = pravegaWriteOperationReader.readNextEvent(READ_TIMEOUT);
        } while (event.isCheckpoint());

        // validate message data
        assertNotNull(event.getEvent());
        assertEquals(json, event.getEvent());
    }

    @Test
    public void testWriteOperation() {
        String json = TestUtils.generateJsonMessage();
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, CREATE_OPERATION_STREAM);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

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
            event = pravegaWriteOperationReader.readNextEvent(READ_TIMEOUT);
        } while (event.isCheckpoint());

        // validate message data
        assertNotNull(event.getEvent());
        assertEquals(json, event.getEvent());
    }

    @Test
    public void testBigMessageWriteOperation() {
        String json = TestUtils.generate2MBmessage();
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, CREATE_OPERATION_STREAM);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

        Map<String, Object> opProps = new HashMap<>();
        opProps.put(Constants.ROUTING_KEY_TYPE_PROPERTY, WriterConfig.RoutingKeyType.Fixed.toString());
        opProps.put(Constants.ROUTING_KEY_PROPERTY, FIXED_ROUTING_KEY);
        tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

        // prep test message
        List<InputStream> inputs = new ArrayList<>();
        inputs.add(new ByteArrayInputStream(json.getBytes()));
        // send the test message through the connector
        List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
        assertEquals("413", actual.get(0).getStatusCode());
        assertEquals(OperationStatus.APPLICATION_ERROR, actual.get(0).getStatus());
        logger.log(Level.INFO, String.format(actual.get(0).getStatusCode()));
        // read from stream and verify event data
        EventRead<String> event;
        do {
            event = pravegaWriteOperationReader.readNextEvent(READ_TIMEOUT);
        } while (event.isCheckpoint());
        // validate message data
        assertEquals(null, event.getEvent());
    }

    @Test
    public void testWriteWithNullRoutingKeyType() {
        String json = TestUtils.generateJsonMessage();
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, CREATE_OPERATION_STREAM);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

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
            event = pravegaWriteOperationReader.readNextEvent(READ_TIMEOUT);
        } while (event.isCheckpoint());

        // validate message data
        assertNotNull(event.getEvent());
        assertEquals(json, event.getEvent());
    }

    @Test
    public void testWriteMultipleDocuments() {
        String[] messages = {TestUtils.generateJsonMessage(), TestUtils.generateJsonMessage(), TestUtils.generateJsonMessage()};
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, CREATE_OPERATION_STREAM);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

        Map<String, Object> opProps = new HashMap<>();
        tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

        // prep test message
        List<InputStream> inputs = new ArrayList<>();
        for (String message : messages) {
            inputs.add(new ByteArrayInputStream(message.getBytes()));
        }

        // send the test message through the connector
        List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);

        assertEquals(messages.length, actual.size());
        for (SimpleOperationResult result : actual) {
            assertEquals("OK", result.getStatusCode());
        }

        // read from stream and verify event data
        for (String message : messages) {
            EventRead<String> event;
            do {
                event = pravegaWriteOperationReader.readNextEvent(READ_TIMEOUT);
            } while (event.isCheckpoint());
            assertNotNull(event.getEvent());
            assertEquals(message, event.getEvent());
        }
    }

    @Test
    public void testReadOperation() throws Exception {
        String json = TestUtils.generateJsonMessage();
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, QUERY_OPERATION_STREAM);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

        Map<String, Object> opProps = new HashMap<>();
        opProps.put(Constants.READER_GROUP_PROPERTY, QUERY_OPERATION_READER_GROUP);
        opProps.put(Constants.READ_TIMEOUT_PROPERTY, 5000L);

        // write test event to stream first
        pravegaReadOperationWriter.writeEvent(json).get();

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

    @Test
    public void testReadWithMultipleDocuments() throws Exception {
        String[] messages = {TestUtils.generateJsonMessage(), TestUtils.generateJsonMessage(), TestUtils.generateJsonMessage()};
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, QUERY_OPERATION_STREAM);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

        Map<String, Object> opProps = new HashMap<>();
        opProps.put(Constants.READER_GROUP_PROPERTY, QUERY_OPERATION_READER_GROUP);
        opProps.put(Constants.READ_TIMEOUT_PROPERTY, 5000L);

        // write test events to stream first
        for (String message : messages) {
            pravegaReadOperationWriter.writeEvent(message).get();
        }

        // execute the connector to read the events
        tester.setOperationContext(OperationType.QUERY, connProps, opProps, null, null);
        List<SimpleOperationResult> results = tester.executeQueryOperation(null);
        assertEquals("OK", results.get(0).getStatusCode());

        // validate message data
        assertEquals(messages.length, results.get(0).getPayloads().size());
        for (int i = 0; i < messages.length; i++) {
            String message = messages[i];
            byte[] payload = results.get(0).getPayloads().get(i);
            // document should match our test message
            assertEquals(message, new String(payload));
        }
    }

    @Test
    public void testMaxReadPerExecution() throws Exception {
        String stream = "connector-test-max-read-time";
        long maxReadTime = 4L; // seconds
        long writeTime = maxReadTime * 10; // background writer is much slower than our reader here

        // create new test stream for this test case
        TestUtils.createStreams(clientConfig, PRAVEGA_SCOPE, stream);

        // use background writer (write for writeTime seconds)
        try (BackgroundStreamWriter backgroundWriter = new BackgroundStreamWriter(pravegaClientFactory, stream)) {
            Thread.sleep(writeTime * 1000);
            backgroundWriter.stop();
            backgroundWriter.waitForAck();
        }

        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, stream);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);


        Map<String, Object> opProps = new HashMap<>();
        opProps.put(Constants.READER_GROUP_PROPERTY, stream + "-readers");
        opProps.put(Constants.READ_TIMEOUT_PROPERTY, ReaderConfig.DEFAULT_READ_TIMEOUT);
        // should read for only maxReadTime seconds
        opProps.put(Constants.MAX_READ_TIME_PER_EXECUTION_PROPERTY, maxReadTime);
        // make sure we don't limit the event count
        opProps.put(Constants.MAX_EVENTS_PER_EXECUTION_PROPERTY, 0L);

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

    @Test
    public void testMaxEventsPerExecution() throws Exception {
        String stream = "connector-test-max-events";
        int maxEvents = 50000;
        int writeEvents = maxEvents * 2;

        // create new test stream for this test case
        TestUtils.createStreams(clientConfig, PRAVEGA_SCOPE, stream);

        // use background writer (write writeEvents events)
        try (BackgroundStreamWriter backgroundWriter = new BackgroundStreamWriter(pravegaClientFactory, stream, writeEvents)) {
            backgroundWriter.waitForMaxEvents();
        }

        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, stream);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

        Map<String, Object> opProps = new HashMap<>();
        opProps.put(Constants.READER_GROUP_PROPERTY, stream + "-readers");
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

    @Test
    public void testPollingListenerOperation() throws Exception {
        String[] messages = new String[30];//{TestUtils.generateJsonMessage(), TestUtils.generateJsonMessage(), TestUtils.generateJsonMessage()};
        for (int i = 0; i < 30; i++) {
            messages[i] = TestUtils.generateJsonMessage(i);
        }
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<>();
        connProps.put(Constants.CONTROLLER_URI_PROPERTY, TestUtils.PRAVEGA_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, PRAVEGA_SCOPE);
        connProps.put(Constants.STREAM_PROPERTY, POLLING_LISTENER_STREAM);
        connProps.put(Constants.INTERVAL, INTERVAL);
        connProps.put(Constants.TIME_UNIT, UNIT);

        Map<String, Object> opProps = new HashMap<>();
        opProps.put(Constants.READER_GROUP_PROPERTY, POLLING_OPERATION_READER_GROUP);
        opProps.put(Constants.READ_TIMEOUT_PROPERTY, 5000L);

        tester.setOperationContext(OperationType.LISTEN, connProps, opProps, null, null);
        PravegaPollingOperationConnection pravegaPollingOperationConnection = new PravegaPollingOperationConnection(tester.getOperationContext());
        PravegaPollingOperation pravegaPollingOperation = new PravegaPollingOperation(pravegaPollingOperationConnection);
        PravegaPollingManagerConnection connection = new PravegaPollingManagerConnection(tester.getOperationContext());
        PravegaPollingManager manager = new PravegaPollingManager(connection);
        SimpleListener simpleListener = new SimpleListener();

        Thread dataGenerator = new Thread(() -> {
            for (String message : messages) {
                try {
                    pravegaPollingListenOperationWriter.writeEvent(message).get();
                    //generating 1 msg per seconds
                    Thread.sleep(1000);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        dataGenerator.start();
        try {
            manager.start();
            pravegaPollingOperation.start(simpleListener, manager);
            Thread.sleep(30000); // poll  the event after a while

        }catch (Throwable t){

        }finally {
            manager.stop();
            pravegaPollingOperation.stop();
        }

        for(int i = 0; i<30; i++){
            assertEquals(messages[i], simpleListener.getNextDocument());
        }
    }

    private static String outputStreamToUtf8String(ByteArrayOutputStream baos) throws IOException {
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    class SimpleListener implements Listener {

        private LinkedBlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>();

        @Override
        public PayloadBatch getBatch() {
            return null;
        }

        public <T> IndexedPayloadBatch<T> getBatch(T index) {
            return null;
        }

        @Override
        public void submit(Payload payload) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                payload.writeTo(baos);
                String output = outputStreamToUtf8String(baos);
                if (output != null) {
                    linkedQueue.add(output);
                    logger.log(Level.INFO, String.format("SUBMIT PAYLOAD"));
                } else {
                    logger.log(Level.INFO, String.format("SUBMIT PAYLOAD NULL"));
                }
            } catch (Exception E) {
                logger.log(Level.INFO, String.format("Got exeption during submit paylaod"));
            }
        }

        @Override
        public void submit(Throwable var1) {

        }

        public Future<ListenerExecutionResult> submit(Payload var1, SubmitOptions var2) {
            return null;
        }

        public String getNextDocument() {
            if(linkedQueue.size() > 0)
                return linkedQueue.remove();
            else return null;
        }

        @Override
        public PayloadMetadata createMetadata() {
            return null;
        }
    }

}
