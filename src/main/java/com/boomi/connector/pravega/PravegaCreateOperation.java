package com.boomi.connector.pravega;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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


public class PravegaCreateOperation extends BaseUpdateOperation {

    private PravegaWriter pravegaWriter;

	protected PravegaCreateOperation(PravegaConnection conn) {
		super(conn);
        pravegaWriter = PravegaWriter.getInstance(this.getContext());
	}

	@Override
	protected void executeUpdate(UpdateRequest request, OperationResponse response) {
    	Logger logger = response.getLogger();

        for (ObjectData input : request) {

            try {
            	String message = inputStreamToString(input.getData());
               	logger.log(Level.INFO, String.format("Writing message: '%s' with routing-key: '%s' to stream '%s / %s'%n",
                        message, pravegaWriter.getRoutingKey(), pravegaWriter.getScope(), pravegaWriter.getStreamName()));

                CompletableFuture writeFuture = pravegaWriter.getWriter().writeEvent(pravegaWriter.getRoutingKey(), message);

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