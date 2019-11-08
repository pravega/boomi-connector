package com.boomi.connector.pravega;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseQueryOperation;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.impl.JavaSerializer;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PravegaQueryOperation extends BaseQueryOperation implements AutoCloseable {
	private PravegaConfig pravegaConfig;
	private ReaderConfig readerConfig;
	private EventStreamReader<String> reader;

	PravegaQueryOperation(PravegaConnection conn) {
        super(conn);
		pravegaConfig = conn.getPravegaConfig();
		readerConfig = ReaderConfig.fromContext(this.getContext());

		// create event reader
		reader = conn.getClientFactory().createReader(UUID.randomUUID().toString(), conn.getReaderGroup(),
				new JavaSerializer<>(), io.pravega.client.stream.ReaderConfig.builder().build());
    }

	@Override
	protected void executeQuery(QueryRequest request, OperationResponse response) {
    	Logger logger = response.getLogger();
		FilterData input = request.getFilter();

		try {
			logger.info(String.format("Reading all the events from %s/%s", pravegaConfig.getScope(), pravegaConfig.getStreamName()));
			EventRead<String> event = null;
			do {
				try {
					event = reader.readNextEvent(readerConfig.getReadTimeout());
					if (event.getEvent() != null) {
						logger.log(Level.INFO, String.format("Read event size: %d", event.getEvent().length()));
						response.addPartialResult(input, OperationStatus.SUCCESS, "OK",
								"OK", ResponseUtil.toPayload(new ByteArrayInputStream(event.getEvent().getBytes())));
					}
				} catch (ReinitializationRequiredException e) {
					// There are certain circumstances where the reader needs to be reinitialized
					// TODO: what needs to be done here?  if we re-initialize, how do we resume?
					logger.log(Level.WARNING, "caught ReinitializationRequiredException - Pravega client needs to be reinitialized", e);
				}
			} while (event != null && event.getEvent() != null);

			logger.log(Level.INFO, String.format("No more events from %s/%s", pravegaConfig.getScope(), pravegaConfig.getStreamName()));
		} catch (Exception e) {
			logger.log(Level.SEVERE, String.format("Error reading from %s/%s", pravegaConfig.getScope(), pravegaConfig.getStreamName()), e);
			ResponseUtil.addExceptionFailure(response, input, e);
		}

        response.finishPartialResult(input);
	}

	@Override
	public void close() {
		if (reader != null) reader.close();
	}

	@Override
    public PravegaConnection getConnection() {
        return (PravegaConnection) super.getConnection();
    }
}
