// Copyright (c) 2018 Boomi, Inc.
package com.boomi.connector.pravega;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Disabled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.boomi.util.DOMUtil;
import com.boomi.util.XMLUtil;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.boomi.connector.api.OperationStatus;
import com.boomi.connector.api.OperationType;
import com.boomi.connector.api.QueryFilter;
import com.boomi.connector.api.Sort;
import com.boomi.connector.testutil.ConnectorTester;
import com.boomi.connector.testutil.QueryFilterBuilder;
import com.boomi.connector.testutil.QueryGroupingBuilder;
import com.boomi.connector.testutil.QuerySimpleBuilder;
import com.boomi.connector.testutil.SimpleOperationResult;
import com.boomi.util.StringUtil;

/**
 * @author Dave Hock
 */
public class PravegaOperationTest 
{

    @Test   
    public void testGetOperation() throws Exception
    {
    	PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<String,Object>();
        connProps.put(Constants.URI_PROPERTY, Constants.DEFAULT_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, Constants.DEFAULT_SCOPE);
        connProps.put(Constants.NAME_PROPERTY, Constants.DEFAULT_STREAM_NAME);
        
        Map<String, Object> opProps = new HashMap<String,Object>();
        opProps.put(Constants.READTIMEOUT_PROPERTY, 5000L);
        
        tester.setOperationContext(OperationType.GET, connProps, opProps, null, null);
        List <SimpleOperationResult> results = tester.executeGetOperation("");
        assertEquals("OK",results.get(0).getStatusCode());
        
        List payloads = results.get(0).getPayloads();
        System.out.println("Number of documents:" + payloads.size());
        //We should get at least one document
        assertTrue(payloads.size()>0);
        byte documentBytes[] = (byte[])payloads.get(0);
        //The first document should match our test message
        assertEquals(Constants.DEFAULT_MESSAGE, new String(documentBytes));
    }
    
    
    @Test   
    public void testCreateOperation() throws Exception
    {
    	PravegaConnector connector = new PravegaConnector();
        ConnectorTester tester = new ConnectorTester(connector);

        Map<String, Object> connProps = new HashMap<String,Object>();
        connProps.put(Constants.URI_PROPERTY, Constants.DEFAULT_CONTROLLER_URI);
        connProps.put(Constants.SCOPE_PROPERTY, Constants.DEFAULT_SCOPE);
        connProps.put(Constants.NAME_PROPERTY, Constants.DEFAULT_STREAM_NAME);
        
        Map<String, Object> opProps = new HashMap<String,Object>();
        opProps.put(Constants.ROUTINGKEY_PROPERTY, Constants.DEFAULT_ROUTING_KEY);
        
        tester.setOperationContext(OperationType.CREATE, connProps, opProps, null, null);
        
        List<InputStream> inputs = new ArrayList<InputStream>();
        inputs.add(new ByteArrayInputStream(Constants.DEFAULT_MESSAGE.getBytes()));
        
        List<SimpleOperationResult> actual = tester.executeCreateOperation(inputs);
        assertEquals("OK", actual.get(0).getStatusCode());
        
    }
    
}
