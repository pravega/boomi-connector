package com.boomi;


import com.boomi.serialization.JsonNodeSerializer;
import com.boomi.util.CommonParams;
import com.boomi.util.Constants;
import com.boomi.util.DataGenerator;
import com.boomi.util.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * A simple example app that uses a Pravega Writer to write to a given scope and stream.
 */
public class JSONWriter {
    // Logger initialization
    private static final Logger LOG = LoggerFactory.getLogger(JSONWriter.class);

    public final String scope;
    public final String streamName;
    public final String dataFile;
    public final URI controllerURI;

    public JSONWriter(String scope, String streamName, URI controllerURI,String dataFile) {
        this.scope = scope;
        this.streamName = streamName;
        this.dataFile = dataFile;
        this.controllerURI = controllerURI;
    }

    public static void main(String[] args) {
        // Get the Program parameters
    	final String scope = "examples";
		final String streamName = "helloStream";
		final String routingKey = "helloRoutingKey";
		final String controllerURI1 = "tcp://10.100.90.232:9090";
		final String message = "hello pravega World";
		final URI controllerURI = URI.create(controllerURI1);
        final String dataFile = CommonParams.getParam(Constants.DATA_FILE);
        JSONWriter ew = new JSONWriter(scope, streamName, controllerURI,dataFile);

        ew.run(routingKey);
    }

    public void run(String routingKey) {

                String streamName = "json-stream";
                ObjectNode message = null;
                // Create client config
                ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
                //  create stream
                System.out.println("start clientConfig==");

                boolean  streamCreated = Utils.createStream(scope, streamName, controllerURI);
                System.out.println("streamCreated==");

                LOG.info(" @@@@@@@@@@@@@@@@ STREAM  =  "+streamName+ "  CREATED = "+ streamCreated);
                // Create EventStreamClientFactory
                try( EventStreamClientFactory clientFactory = EventStreamClientFactory.withScope(scope, clientConfig);

                // Create  Pravega event writer
                EventStreamWriter<JsonNode> writer = clientFactory.createEventWriter(
                        streamName,
                        new JsonNodeSerializer(),
                        EventWriterConfig.builder().build())) {
                    //  Coverst CSV  data to JSON
                    String data = DataGenerator.convertCsvToJson(dataFile);
                    System.out.println("data===="+data);

                    // Deserialize the JSON message.
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonArray = objectMapper.readTree(data);
                    System.out.println("jsonArray=="+jsonArray);

                    if (jsonArray.isArray()) {
                        for (JsonNode node : jsonArray) {
                            message = (ObjectNode) node;
                            LOG.info("@@@@@@@@@@@@@ DATA  @@@@@@@@@@@@@  "+message.toString());
                            final CompletableFuture writeFuture = writer.writeEvent(routingKey, message);
                            writeFuture.get();
                            Thread.sleep(10000);
                        }

                    }

        }
        catch (Exception e) {
            LOG.error("@@@@@@@@@@@@@ ERROR  @@@@@@@@@@@@@  "+e.getMessage());
        }

    }
}

