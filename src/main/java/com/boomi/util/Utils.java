/*
*  Copyright 2019 Accenture. All Rights Reserved.
*  The trademarks used in these materials are the properties of their respective owners.
*  This work is protected by copyright law and contains valuable trade secrets and
*  confidential information.
*/
package com.boomi.util;

import io.pravega.client.ClientConfig;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.Stream;
import io.pravega.client.stream.StreamConfiguration;

import java.net.URI;

import com.boomi.model.ConnectionModel;
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
