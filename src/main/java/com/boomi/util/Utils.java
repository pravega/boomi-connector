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
package com.boomi.util;

import java.net.URI;

import io.pravega.client.ClientConfig;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.StreamConfiguration;
/**
 * 
 * @author kritika.b.verma
 *
 */
public class Utils {

	/**
	 * 
	 * @param scope
	 * @param streamName
	 * @param controllerURI
	 * @return
	 */
	public static boolean createStream(String scope, String streamName, URI controllerURI) {
		boolean result = false;
		// Create client config
		ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
		try (StreamManager streamManager = StreamManager.create(clientConfig);) {
			if (CommonParams.isPravegaStandalone()) {
				streamManager.createScope(scope);
			}
			result = streamManager.createStream(scope, streamName, StreamConfiguration.builder().build());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}
}
