// Copyright (c) 2019 Boomi, Inc.
package io.pravega.connector.boomi;

import com.boomi.connector.util.listen.BasePollingManager;

/**
 * Simple {@link BasePollingManager} implementation. There is no additional state to manage in this example beyond
 * what's managed by {@link BasePollingManager}.
 */
public class PollingManager extends BasePollingManager {

    protected PollingManager(PollingManagerConnection connection) {
        super(connection, connection.getInterval(), connection.getUnit());
    }

    /**
     * Starts the manager.
     */
    @Override
    protected void doStart() {

    }

    /**
     * Stops the manager.
     */
    @Override
    protected void doStop() {

    }

}
