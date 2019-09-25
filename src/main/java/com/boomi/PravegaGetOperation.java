/*
*  Copyright 2019 Accenture. All Rights Reserved.
*  The trademarks used in these materials are the properties of their respective owners.
*  This work is protected by copyright law and contains valuable trade secrets and
*  confidential information.
*/

package com.boomi;

import java.util.UUID;
import java.util.logging.Logger;
import com.boomi.connector.api.GetRequest;
import com.boomi.connector.api.ObjectIdData;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.util.BaseGetOperation;
import com.boomi.constants.PravegaConstants;

import io.pravega.client.ClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.ReaderGroupConfig;
import io.pravega.client.stream.ReinitializationRequiredException;
import io.pravega.client.stream.Stream;
import io.pravega.client.stream.impl.JavaSerializer;

/**
 * 
 * @author kritika.b.verma
 *
 */
public class PravegaGetOperation extends BaseGetOperation {

	private static final int READER_TIMEOUT_MS = 10000;

	protected PravegaGetOperation(PravegaConnection conn) {
		super(conn);
	}

	private Logger logger = Logger.getLogger(PravegaGetOperation.class.getName());

	/**
	 * 
	 */

	@Override
	protected void executeGet(GetRequest request, OperationResponse response) {
		PravegaConnection pravegaConnection = getConnection();
		ObjectIdData data = request.getObjectId();
		final String readerGroup = UUID.randomUUID().toString().replace("-", "");
		final ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
				.stream(Stream.of(pravegaConnection.getScope(), pravegaConnection.getStreamName())).build();
		try (ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(pravegaConnection.getScope(),
				pravegaConnection.getControllerURI())) {
			readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);
		}

		try (ClientFactory clientFactory = ClientFactory.withScope(pravegaConnection.getScope(),
				pravegaConnection.getControllerURI());
				EventStreamReader<String> reader = clientFactory.createReader("reader", readerGroup,
						new JavaSerializer<String>(), ReaderConfig.builder().build())) {
			EventRead<String> event = null;
			do {
				try {
					event = reader.readNextEvent(READER_TIMEOUT_MS);
					if (event.getEvent() != null) {
						System.out.format("Read event '%s'%n", event.getEvent());
					}
				} catch (ReinitializationRequiredException e) {
					e.printStackTrace();
					break;
				}
			} while (event.getEvent() != null);
			response.addResult(data, OperationStatus.SUCCESS, PravegaConstants.STATUS_CODE_SUCCESS, event.getEvent(),
					null);
		}
	}

	/**
	 * 
	 */
	@Override
	public PravegaConnection getConnection() {
		return (PravegaConnection) super.getConnection();
	}
}