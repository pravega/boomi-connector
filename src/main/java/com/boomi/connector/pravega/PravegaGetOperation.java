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

import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.ReinitializationRequiredException;


public class PravegaGetOperation extends BaseGetOperation {

    private PravegaReader pravegaReader;

    protected PravegaGetOperation(PravegaConnection conn) {
        super(conn);

        pravegaReader = PravegaReader.getInstance(this.getContext());

    }

	@Override
	protected void executeGet(GetRequest request, OperationResponse response) {
    	Logger logger = response.getLogger();
        ObjectIdData input = request.getObjectId();

        logger.log(Level.INFO, String.format("Reading all the events from %s/%s%n", pravegaReader.getScope(), pravegaReader.getStreamName()));
		EventRead<String> event = null;
		do {
			try {
				event = pravegaReader.getReader().readNextEvent(pravegaReader.getReadTimeout());
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
		
		logger.log(Level.INFO, String.format("No more events from %s/%s%n", pravegaReader.getScope(), pravegaReader.getStreamName()));
        

        response.finishPartialResult(input);
	}

	@Override
    public PravegaConnection getConnection() {
        return (PravegaConnection) super.getConnection();
    }
}