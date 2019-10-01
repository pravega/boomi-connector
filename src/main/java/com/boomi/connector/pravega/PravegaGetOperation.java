package com.boomi.connector.pravega;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.boomi.connector.api.GetRequest;
import com.boomi.connector.api.ObjectIdData;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.api.ResponseUtil;
import com.boomi.connector.util.BaseGetOperation;
import com.boomi.util.IOUtil;

import io.pravega.client.ClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.ReaderGroupConfig;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.Stream;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.JavaSerializer;

public class PravegaGetOperation extends BaseGetOperation {

    protected PravegaGetOperation(PravegaConnection conn) {
        super(conn);
    }

	@Override
	protected void executeGet(GetRequest request, OperationResponse response) {
    	Logger logger = response.getLogger();
        ObjectIdData input = request.getObjectId();

        Map<String, Object> connProps = this.getContext().getConnectionProperties();
        URI controllerURI = URI.create((String)connProps.get(Constants.URI_PROPERTY));
        String scope = (String)connProps.get(Constants.SCOPE_PROPERTY);
        String streamName = (String)connProps.get(Constants.NAME_PROPERTY);

        Map<String, Object> opProps = this.getContext().getOperationProperties();
        Long readTimeout = (Long)opProps.get(Constants.READTIMEOUT_PROPERTY);
        
        final String readerGroup = UUID.randomUUID().toString().replace("-", "");
        final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(scope, streamName))
                .build();
        try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(scope, controllerURI)) {
            readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);
        }

        ClientFactory clientFactory = ClientFactory.withScope(scope, controllerURI);
        
        EventStreamReader<String> reader = clientFactory.createReader("reader",
                readerGroup,
                new JavaSerializer<String>(),
                ReaderConfig.builder().build());
        
        logger.log(Level.INFO, String.format("Reading all the events from %s/%s%n", scope, streamName));
		EventRead<String> event = null;
		do {
			try {
				event = reader.readNextEvent(readTimeout);
				if (event.getEvent() != null) {
					logger.log(Level.INFO, String.format("Read event '%s'%n", event.getEvent()));
		            response.addPartialResult(input, OperationStatus.SUCCESS, "OK",
		                    "OK", ResponseUtil.toPayload(new ByteArrayInputStream(event.getEvent().getBytes())));
//		            ResponseUtil.addPartialSuccess(response, input, httpResponse.getResponseCodeAsString(), ResponseUtil.toPayload(is)); 
				}
			} catch (ReinitializationRequiredException e) {
				// There are certain circumstances where the reader needs to be reinitialized
				e.printStackTrace();
			}
		} while (event.getEvent() != null);
		
		logger.log(Level.INFO, String.format("No more events from %s/%s%n", scope, streamName));
        
        try {
            // read event
            // dump the results into the response

       }
        catch (Exception e) {
            ResponseUtil.addExceptionFailure(response, input, e);
        }
        response.finishPartialResult(input);
	}

	@Override
    public PravegaConnection getConnection() {
        return (PravegaConnection) super.getConnection();
    }
}