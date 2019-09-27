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

import io.pravega.client.admin.StreamManager;
/**
 * 
 * @author kritika.b.verma
 *
 */
public class ScopeCreator {

    private static Logger LOG = LoggerFactory.getLogger(ScopeCreator.class);    
/**
 * 
 * @param scope
 * @param controllerURI
 */
    public void createScope(String  scope, URI controllerURI ) {
        try(StreamManager streamManager = StreamManager.create(controllerURI);) {
            final boolean scopeIsNew = streamManager.createScope(scope);
            if (scopeIsNew) {
                LOG.info("succeed in creating scope  '"+scope);
            }
            else
            {
                LOG.info("already exists scope  '"+scope);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
