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


import static com.boomi.constants.PravegaConstants.CONTROLLER_URI;
import static com.boomi.constants.PravegaConstants.STREAM_NAME;
import static com.boomi.constants.PravegaConstants.STREAM_SCOPE;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.PropertyMap;
import com.boomi.connector.util.BaseConnection;
/**
 * 
 * @author kritika.b.verma
 *
 */
public class PravegaConnection extends BaseConnection {

	private static Logger LOG = LoggerFactory.getLogger(PravegaConnection.class);
	private String host;
	public  String scope;
    public  String streamName;
    public  URI controllerURI;
    private String message;
    private String routingKey="";
    private ScopeCreator scopecreater;
    private StreamCreator streamcreater;

    /**
     * 
     * @param context
     */
	public PravegaConnection(BrowseContext context) {
		super(context);
		PropertyMap propertyMap = context.getConnectionProperties();
		host = propertyMap.getProperty(CONTROLLER_URI);	
		streamName = propertyMap.getProperty(STREAM_NAME);
		scope = propertyMap.getProperty(STREAM_SCOPE);
		controllerURI = URI.create(host);		
		
		if(scopecreater == null) {
			
			scopecreater = new ScopeCreator();
		}
		if(streamcreater == null) {
			streamcreater = new StreamCreator();
		}
		
		runPravega(scope, streamName, controllerURI);
		
	}
	

/**
 * 
 * @param scope
 * @param streamName
 * @param controllerURI
 * @throws PravegaConnectException 
 */
	
	public void runPravega(String scope, String streamName ,URI controllerURI ){
			scopecreater.createScope(scope , controllerURI);
			streamcreater.createStream(scope , streamName, controllerURI);
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public URI getControllerURI() {
		return controllerURI;
	}

	public void setControllerURI(URI controllerURI) {
		this.controllerURI = controllerURI;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ScopeCreator getScopecreater() {
		return scopecreater;
	}

	public void setScopecreater(ScopeCreator scopecreater) {
		this.scopecreater = scopecreater;
	}

	public StreamCreator getStreamcreater() {
		return streamcreater;
	}

	public void setStreamcreater(StreamCreator streamcreater) {
		this.streamcreater = streamcreater;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}	

}