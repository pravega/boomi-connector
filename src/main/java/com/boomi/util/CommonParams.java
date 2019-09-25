/*
*  Copyright 2019 Accenture. All Rights Reserved.
*  The trademarks used in these materials are the properties of their respective owners.
*  This work is protected by copyright law and contains valuable trade secrets and
*  confidential information.
*/
package com.boomi.util;

import java.net.URI;

import org.apache.flink.api.java.utils.ParameterTool;

/**
 * 
 * @author kritika.b.verma
 *
 */
public class CommonParams {

	/**
	 * 
	 * @return
	 */
	public static URI getControllerURI() {
        return URI.create(getEnvVar("PRAVEGA_CONTROLLER_URI", "tcp://10.100.90.232:9090"));
    }
/**
 * 
 * @return
 */
    public static boolean  isPravegaStandalone() {
        return Boolean.parseBoolean(getDefaultParam(Constants.PRAVEGA_STANDALONE));
    }
/**
 * 
 * @return
 */
    public static String getUser() {
        return getEnvVar("PRAVEGA_STANDALONE_USER", "admin");
    }
/**
 * 
 * @return
 */
    public static String getPassword() {
        return getEnvVar("PRAVEGA_STANDALONE_PASSWORD", "1111_aaaa");
    }
/**
 * 
 * @return
 */
    public static String getScope() {
        return getEnvVar("PRAVEGA_SCOPE", "workshop-samples");
    }
/**
 * 
 * @return
 */
    public static String getStreamName() {
        return getEnvVar("STREAM_NAME", "workshop-stream");
    }
/**
 * 
 * @return
 */
    public static String getRoutingKeyAttributeName() {
        return getEnvVar("ROUTING_KEY_ATTRIBUTE_NAME", "test");
    }
/**
 * 
 * @return
 */
    public static String getMessage() {
        return getEnvVar("MESSAGE", "This is Nautilus OE team workshop samples.");
    }
/**
 * 
 * @param name
 * @param defaultValue
 * @return
 */
    private static String getEnvVar(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }
/**
 * 
 * @return
 */
    public static URI getGatewayURI() {
        return URI.create(getEnvVar("GATEWAY_URI", "http://0.0.0.0:3000/"));
    }
/**
 * 
 * @return
 */
    public static int getTargetRateEventsPerSec() {
        return Integer.parseInt(getEnvVar("PRAVEGA_TARGET_RATE_EVENTS_PER_SEC", "100"));
    }
/**
 * 
 * @return
 */
    public static int getScaleFactor() {
        return Integer.parseInt(getEnvVar("PRAVEGA_SCALE_FACTOR", "2"));
    }
/**
 * 
 * @return
 */
    public static int getMinNumSegments() {
        return Integer.parseInt(getEnvVar("PRAVEGA_MIN_NUM_SEGMENTS", "1"));
    }
/**
 * 
 * @return
 */
    public static int getListenPort() {
        return Integer.parseInt(getEnvVar("LISTEN_PORT", "54672"));
    }
/**
 * 
 */
    public static ParameterTool params = null;
    public static void init(String[] args)
    {
        params = ParameterTool.fromArgs(args);
    }
/**
 * 
 * @param key
 * @return
 */
    public static String getParam(String key)
    {
        if(params != null && params.has(key))
        {
            return params.get(key);
        }
        else
        {
            return  getDefaultParam(key);
        }
    }
/**
 * 
 * @param key
 * @return
 */
    private static String  getDefaultParam(String key)
    {
        String keyValue = null;
        if(key != null)
        {
           switch (key) {
                case "pravega_scope":
                    keyValue = "workshop-samples";
                    break;
                case "stream_name":
                    keyValue = "workshop-stream";
                    break;
                case "pravega_controller_uri":
                    keyValue = "tcp://10.100.90.232:9090";
                    break;
                case "routing_key_attribute_name":
                    keyValue = "100";
                    break;
                case "pravega_standalone":
                    keyValue = "true";
                    break;
               case "data_file":
                   keyValue = "earthquakes1970-2014.csv";
                   break;
               case "message":
                   keyValue = "To demonstrate Nautilus streams sending a string message";
                   break;
                default:
                    keyValue = null;
            }
        }
        return keyValue;
    }
}
