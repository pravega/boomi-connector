/*
*  Copyright 2019 Accenture. All Rights Reserved.
*  The trademarks used in these materials are the properties of their respective owners.
*  This work is protected by copyright law and contains valuable trade secrets and
*  confidential information.
*/

package com.boomi;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boomi.model.ConnectionModel;
import com.boomi.util.Utils;

/**
 * A simple example app that creates stream in Pravega
 */
public class StreamCreator {

    private static Logger LOG = LoggerFactory.getLogger(StreamCreator.class);  

    public void createStream(String scope, String streamName, URI controllerURI) {
        //  create stream
        boolean  streamCreated = Utils.createStream(scope, streamName, controllerURI);
        LOG.info(" @@@@@@@@@@@@@@@@ STREAM  =  "+streamName+ "  CREATED = "+ streamCreated);
        if (streamCreated) {
            LOG.info("succeed in creating stream '%s' under scope '%s'", streamName, scope);
        }
    }

}
