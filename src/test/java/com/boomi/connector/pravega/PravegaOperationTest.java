// Copyright (c) 2018 Boomi, Inc.
package com.boomi.connector.pravega;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.junit.jupiter.api.Test;
import com.boomi.connector.api.OperationType;
import com.boomi.connector.testutil.ConnectorTester;
import com.boomi.connector.testutil.SimpleOperationResult;


/**
 * @author Dave Hock
 * @author Mohammad Omar Faruk
 */
public class PravegaOperationTest 
{

    @Test
    public void testCreateOperationPravegaWriter() throws Exception
    {
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<String,Object>();
        connProps.put(Constants.URI_PROPERTY, Constants.DEFAULT_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, Constants.DEFAULT_SCOPE);
        connProps.put(Constants.NAME_PROPERTY, Constants.DEFAULT_STREAM_NAME);

        Map<String, Object> opProps = new HashMap<String,Object>();
        opProps.put(Constants.FIXED_ROUTINGKEY_PROPERTY, Constants.DEFAULT_ROUTING_KEY);
        opProps.put(Constants.ROUTINGKEY_CONFIG_VALUE_PROPERTY, Constants.DEFAULT_ROUTING_CONFIG_VALUE);
        opProps.put(Constants.ROUTINGKEY_NEEDED_PROPERTY, true);
        tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);

        List<InputStream> inputs = new ArrayList<InputStream>();
        inputs.add(new ByteArrayInputStream(Constants.DEFAULT_JSON_MESSAGE.getBytes()));

        List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
        assertEquals("OK", actual.get(0).getStatusCode());

    }



    @Test
    public  void testGetOperationPravegaReader() throws Exception
    {
        PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<String,Object>();
        connProps.put(Constants.URI_PROPERTY, Constants.DEFAULT_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, Constants.DEFAULT_SCOPE);
        connProps.put(Constants.NAME_PROPERTY, Constants.DEFAULT_STREAM_NAME);

        Map<String, Object> opProps = new HashMap<String,Object>();
        opProps.put(Constants.READTIMEOUT_PROPERTY, 5000L);
        opProps.put(Constants.ROUTINGKEY_CONFIG_VALUE_PROPERTY, Constants.DEFAULT_ROUTING_CONFIG_VALUE);
        opProps.put(Constants.ROUTINGKEY_NEEDED_PROPERTY, true);

        tester.setOperationContext(OperationType.GET, connProps, opProps, null, null);
        List <SimpleOperationResult> results = tester.executeGetOperation("");
        assertEquals("OK",results.get(0).getStatusCode());

        List payloads = results.get(0).getPayloads();
        System.out.println("Number of documents:" + payloads.size());
        //We should get at least one document
        assertTrue(payloads.size()>0);
        for(int i =0; i<payloads.size(); i++){
            byte documentBytes[] = (byte[])payloads.get(i);
            //The first document should match our test message
            assertEquals(Constants.DEFAULT_JSON_MESSAGE, new String(documentBytes));
        }


    }

    
}
