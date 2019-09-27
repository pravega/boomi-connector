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

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boomi.util.Utils;
/**
 * 
 * @author kritika.b.verma
 *
 */
public class StreamCreator {

    private static Logger LOG = LoggerFactory.getLogger(StreamCreator.class);  
/**
 * 
 * @param scope
 * @param streamName
 * @param controllerURI
 */
    public void createStream(String scope, String streamName, URI controllerURI) {
        //  create stream
        boolean  streamCreated = Utils.createStream(scope, streamName, controllerURI);
        LOG.info(" @@@@@@@@@@@@@@@@ STREAM  =  "+streamName+ "  CREATED = "+ streamCreated);
        if (streamCreated) {
            LOG.info("succeed in creating stream '%s' under scope '%s'", streamName, scope);
        }
    }

}
