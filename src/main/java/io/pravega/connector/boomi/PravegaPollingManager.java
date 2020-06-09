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

import com.boomi.connector.util.listen.BasePollingManager;

/**
 * Simple {@link BasePollingManager} implementation. There is no additional state to manage in this example beyond
 * what's managed by {@link BasePollingManager}.
 */
public class PravegaPollingManager extends BasePollingManager {

    protected PravegaPollingManager(PravegaPollingManagerConnection connection) {
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
