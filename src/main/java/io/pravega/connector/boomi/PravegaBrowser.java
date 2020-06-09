/*
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package io.pravega.connector.boomi;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseBrowser;

public class PravegaBrowser extends BaseBrowser implements ConnectionTester {
    public PravegaBrowser(BrowseContext browseContext) {
        super(browseContext);
    }

    @Override
    public ObjectDefinitions getObjectDefinitions(java.lang.String objectTypeId, java.util.Collection<ObjectDefinitionRole> roles) {
        return null;
    }

    @Override
    public ObjectTypes getObjectTypes() {
        return null;
    }

    @Override
    public void testConnection() {
        try {
            PravegaUtil.testConnection(getContext());
        } catch (Throwable t) {
            throw new ConnectorException("Could not initialize connection to Pravega", t);
        }
    }
}
