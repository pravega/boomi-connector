package com.boomi.connector.pravega;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseBrowser;

public class PravegaBrowser extends BaseBrowser implements ConnectionTester {

    public PravegaBrowser(PravegaConnection conn){
        super(conn);
    }

    @Override
    public ObjectDefinitions getObjectDefinitions(java.lang.String objectTypeId, java.util.Collection<ObjectDefinitionRole> roles){
        return null;
    }

    @Override
    public ObjectTypes getObjectTypes(){

        return null;
    }

    @Override
    public void testConnection(){
        try {
            PravegaConnection connection = new PravegaConnection(getContext());

        }catch (Exception e){
            throw new ConnectorException("Could not establish a connection");
        }
    }
}
