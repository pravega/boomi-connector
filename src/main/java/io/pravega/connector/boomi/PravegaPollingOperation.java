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

import com.boomi.connector.api.Payload;
import com.boomi.connector.util.listen.BasePollingOperation;

/**
 * Simple {@link BasePollingOperation} implementation. When polled, this operation executes a request and converts the
 * response to 1 or more {@link Payload} instances.
 */
public class PravegaPollingOperation extends BasePollingOperation<PravegaPollingManager> {

    /**
     * Creates a new instance using the provided connection
     */
    protected PravegaPollingOperation(PravegaPollingOperationConnection connection) {
        super(connection);
    }

    /**
     * Starts the polling operation. If shared state is
     * required, the provided manager can be used to share state.
     *
     * @param manager the associated manager
     */
    @Override
    protected void doStart(PravegaPollingManager manager) {
        getConnection().open();
    }

    /**
     * Stops the polling operation.
     */
    @Override
    protected void doStop() {
        getConnection().close();
    }

    /**
     * Performs the poll by using the connection to execute a request
     */
    @Override
    protected Iterable<Payload> doPoll() {
        return getConnection().executePayloadRequest();
    }

    /**
     * The base operation classes don't yet support generic connections but you can safely override
     * {@link #getConnection()} to return the proper type because it's constrained by the constructor.
     */
    @Override
    public PravegaPollingOperationConnection getConnection() {
        return (PravegaPollingOperationConnection) super.getConnection();
    }

}
