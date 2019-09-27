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

import java.util.Collection;
import java.util.logging.Logger;

import com.boomi.connector.api.ConnectionTester;
import com.boomi.connector.api.ConnectorException;
import com.boomi.connector.api.ContentType;
import com.boomi.connector.api.ObjectDefinition;
import com.boomi.connector.api.ObjectDefinitionRole;
import com.boomi.connector.api.ObjectDefinitions;
import com.boomi.connector.api.ObjectType;
import com.boomi.connector.api.ObjectTypes;
import com.boomi.connector.util.BaseBrowser;

/**
 * 
 * @author kritika.b.verma
 *
 */
public class PravegaBrowser extends BaseBrowser implements ConnectionTester {

	private Logger logger = Logger.getLogger(PravegaBrowser.class.getName());
	public String streamName;

	protected PravegaBrowser(PravegaConnection conn) {
		super(conn);
	}

	/**
	 * r
	 */
	
	  @Override 
	  public ObjectDefinitions getObjectDefinitions(String objectTypeId, Collection<ObjectDefinitionRole> roles) { 
		  ObjectDefinition objectDefinition =  new ObjectDefinition(); objectDefinition.setElementName("");
			  objectDefinition.setInputType(ContentType.NONE);
			  objectDefinition.setOutputType(ContentType.NONE);			  
			  ObjectDefinitions definitions = new ObjectDefinitions();			  
			  definitions.getDefinitions().add(objectDefinition); 
			  return definitions;	  
	  }
	
	/**
	 * 
	 */
	@Override
	public ObjectTypes getObjectTypes() {
		ObjectTypes types = new ObjectTypes();
		ObjectType responseType = new ObjectType();
		responseType.setId(getConnection().getStreamName());
		types.getTypes().add(responseType);

		return types;
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
	 */
	@Override
	public void testConnection() {
		PravegaConnection connection = null;
		logger.info("Trying to connect to the Pravega");

		try {
			connection = getConnection();
			logger.info("Testing connection");

		} catch (Exception e) {
			logger.severe("Exception occured while testing connection - " + e.getMessage());
			throw new ConnectorException(e);
		} finally {
			if (null != getConnection()) {

			}
		}
	}

}