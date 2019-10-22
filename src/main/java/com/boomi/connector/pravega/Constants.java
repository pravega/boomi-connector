/*
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 */
package com.boomi.connector.pravega;

/**
 * Defines a handful of constants shared by classes in this package.
 *
 */
public class Constants {
    protected static final String DEFAULT_SCOPE = "examplesBoomi";
    protected static final String DEFAULT_STREAM_NAME = "helloBoomiStream";
    protected static final String DEFAULT_CONTROLLER_URI = "tcp://127.0.0.1:9090";
    
    protected static final String DEFAULT_ROUTING_KEY = "helloBoomiRoutingKey";
    protected static final String DEFAULT_ROUTING_CONFIG_VALUE = "salary";
    protected static final String DEFAULT_MESSAGE = "hello boomi world 19";
    protected static final String DEFAULT_JSON_MESSAGE = "{\"name\":\"sonoo\",\"salary\":600000.0,\"age\":27}";
    protected static final boolean DEFAULT_IS_ROUTING_KEY_NEEDED = true;
    
    protected static final String URI_PROPERTY = "uri";
    protected static final String SCOPE_PROPERTY = "scope";
    protected static final String NAME_PROPERTY = "name";

    protected static final String READTIMEOUT_PROPERTY = "readTimeout";
    protected static final String FIXED_ROUTINGKEY_PROPERTY = "fixedRoutingKey";
    protected static final String ROUTINGKEY_NEEDED_PROPERTY = "routingKeyNeeded";
    protected static final String ROUTINGKEY_CONFIG_VALUE_PROPERTY = "routingKeyConfigValue";
}