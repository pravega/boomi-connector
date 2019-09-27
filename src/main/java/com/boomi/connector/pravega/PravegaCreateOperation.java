package com.boomi.connector.pravega;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.boomi.connector.api.ObjectData;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.api.ResponseUtil;
import com.boomi.connector.api.UpdateRequest;
import com.boomi.connector.util.BaseUpdateOperation;
import com.boomi.util.IOUtil;
import java.net.URI;

import io.pravega.client.ClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.JavaSerializer;

public class PravegaCreateOperation extends BaseUpdateOperation {

	protected PravegaCreateOperation(PravegaConnection conn) {
		super(conn);
	}

	@Override
	protected void executeUpdate(UpdateRequest request, OperationResponse response) {
    	Logger logger = response.getLogger();
        
        Map<String, Object> connProps = this.getContext().getConnectionProperties();
        URI controllerURI = URI.create((String)connProps.get(Constants.URI_PROPERTY));
        String scope = (String)connProps.get(Constants.SCOPE_PROPERTY);
        String streamName = (String)connProps.get(Constants.NAME_PROPERTY);

        Map<String, Object> opProps = this.getContext().getOperationProperties();
        String routingKey = (String)opProps.get(Constants.ROUTINGKEY_PROPERTY);

        ClientFactory clientFactory = ClientFactory.withScope(scope, controllerURI);
        
        EventStreamWriter<String> writer = clientFactory.createEventWriter(streamName,
                                                                                 new JavaSerializer<String>(),
                                                                                 EventWriterConfig.builder().build());
            

        for (ObjectData input : request) {

            try {
            	String message = inputStreamToString(input.getData());
               	logger.log(Level.INFO, String.format("Writing message: '%s' with routing-key: '%s' to stream '%s / %s'%n",
                        message, routingKey, scope, streamName));

                CompletableFuture writeFuture = writer.writeEvent(routingKey, message);

                // dump the results into the response
                response.addResult(input, OperationStatus.SUCCESS, "OK",
                        "OK", ResponseUtil.toPayload(new ByteArrayInputStream("".getBytes())));
  
  
            }
            catch (Exception e) {
                // make best effort to process every input
                ResponseUtil.addExceptionFailure(response, input, e);
            }
        }                                                                                 
    }

	@Override
    public PravegaConnection getConnection() {
        return (PravegaConnection) super.getConnection();
    }
	
    static String inputStreamToString(InputStream is) throws IOException
    {
    	try (Scanner scanner = new Scanner(is, "UTF-8")) {
    		return scanner.useDelimiter("\\A").next();
    	}
    }
}