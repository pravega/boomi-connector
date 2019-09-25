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

import io.pravega.client.admin.StreamManager;

public class ScopeCreator {

    private static Logger LOG = LoggerFactory.getLogger(ScopeCreator.class);    

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
