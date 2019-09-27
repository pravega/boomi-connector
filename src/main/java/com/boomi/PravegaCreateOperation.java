/*
 * Copyright (c) 2017 Dell Inc. and Accenture, or its subsidiaries. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 */
package com.boomi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boomi.connector.api.ObjectData;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.api.Payload;
import com.boomi.connector.api.PayloadUtil;
import com.boomi.connector.api.ResponseUtil;
import com.boomi.connector.api.UpdateRequest;
import com.boomi.connector.util.BaseUpdateOperation;
import com.boomi.constants.PravegaConstants;
import com.google.common.base.Charsets;

import io.pravega.client.ClientFactory;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.impl.JavaSerializer;

/**
 * 
 * @author kritika.b.verma
 *
 */
public class PravegaCreateOperation extends BaseUpdateOperation {

	protected PravegaCreateOperation(PravegaConnection conn) {
		super(conn);
	}

	private static Logger LOG = LoggerFactory.getLogger(PravegaCreateOperation.class);

	/**
	 * 
	 */
	@Override
	protected void executeUpdate(UpdateRequest request, OperationResponse response) {
		InputStream input = null;
		Payload payload = null;
		for (ObjectData data : request) {
			input = data.getData();
			PravegaConnection pravegaConnection = getConnection();

			try (ClientFactory clientFactory = ClientFactory.withScope(pravegaConnection.getScope(),
					pravegaConnection.getControllerURI());
					EventStreamWriter<String> writer = clientFactory.createEventWriter(
							pravegaConnection.getStreamName(), new JavaSerializer(),
							EventWriterConfig.builder().build())) {
				String message = convert(input, Charsets.UTF_8);
				if (message != null) {
					final CompletableFuture writeFuture = writer.writeEvent(pravegaConnection.getRoutingKey(), message);

					if (writeFuture != null) {
						writeFuture.get();
						LOG.info("Writing message: '%s' with routing-key: '%s' to stream '%s / %s'%n", message,
								pravegaConnection.getRoutingKey(), pravegaConnection.getScope(),
								pravegaConnection.getStreamName());

//						response.addResult(data, OperationStatus.SUCCESS, PravegaConstants.STATUS_CODE_SUCCESS, message,
//								null);
						
						payload = PayloadUtil
								.toPayload(new StringBuilder("Writing ").append(" message to pravega stream successfully-")
										.append(pravegaConnection.getStreamName()).append(message).toString());
						ResponseUtil.addSuccess(response, data, payload.toString());
					} else {
						String errMsg = new StringBuilder("Error ").append(" while writing message to pravega stream ")
								.toString();
						ResponseUtil.addFailure(response, data, PayloadUtil.toPayload(errMsg).toString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 
	 */
	@Override
	public PravegaConnection getConnection() {
		return (PravegaConnection) super.getConnection();
	}

	/**
	 * 
	 * @param inputStream
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public String convert(InputStream inputStream, Charset charset) throws IOException {

		StringBuilder stringBuilder = new StringBuilder();
		String line = null;

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}

		return stringBuilder.toString();
	}
}