/*
*  Copyright 2019 Accenture. All Rights Reserved.
*  The trademarks used in these materials are the properties of their respective owners.
*  This work is protected by copyright law and contains valuable trade secrets and
*  confidential information.
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

public class PravegaBrowser extends BaseBrowser implements ConnectionTester{
	
	
	private Logger logger = Logger.getLogger(PravegaBrowser.class.getName());	 
	public String streamName;
    protected PravegaBrowser(PravegaConnection conn) {
        super(conn);
    }
    //

	@Override
	public ObjectDefinitions getObjectDefinitions(String objectTypeId,
			Collection<ObjectDefinitionRole> roles) {
        ObjectDefinition objectDefinition = new ObjectDefinition();
        objectDefinition.setElementName("");
        objectDefinition.setInputType(ContentType.NONE);
        objectDefinition.setOutputType(ContentType.NONE);

        ObjectDefinitions definitions = new ObjectDefinitions();

        definitions.getDefinitions().add(objectDefinition);
        return definitions;

	private Logger logger = Logger.getLogger(PravegaBrowser.class.getName());
	public String streamName;

	protected PravegaBrowser(PravegaConnection conn) {
		super(conn);
	}

	/**
	 * 
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
		logger.info("Trying to connect to the MongoDB");

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