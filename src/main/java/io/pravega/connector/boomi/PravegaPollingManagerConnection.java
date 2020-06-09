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

import com.boomi.connector.api.ConnectorContext;
import com.boomi.connector.util.BaseConnection;

import java.util.concurrent.TimeUnit;

/**
 * {@link BaseConnection} implementation for use with {@link PravegaPollingManager} instances. This connection provides
 * as an abstraction for relevant connection properties. Since this connection is based on a {@link ConnectorContext},
 * the connection cannot access operation specific details (e.g., operation properties).
 */
public class PravegaPollingManagerConnection extends BaseConnection<ConnectorContext> {

    private PravegaConfig pravegaConfig;

    /**
     * Creates a new instance using the provided connector context
     */
    public PravegaPollingManagerConnection(ConnectorContext context) {
        super(context);
        pravegaConfig = new PravegaConfig(context);
    }

    /**
     * Returns the polling interval specified by the user in the connection settings.
     *
     * @return the interval
     */
    public long getInterval() {
        return pravegaConfig.getInterval();
    }

    /**
     * Returns the polling time unit specified by the user in the connection settings.
     *
     * @return the unit
     */
    public TimeUnit getUnit() {
        return pravegaConfig.getUnit();
    }

}
