// Copyright (c) 2019 Boomi, Inc.
package io.pravega.connector.boomi;

import com.boomi.connector.api.Payload;
import com.boomi.connector.util.listen.BasePollingOperation;

/**
 * Simple {@link BasePollingOperation} implementation. When polled, this operation executes a request and converts the
 * response to 1 or more {@link Payload} instances.
 */
public class PollingOperation extends BasePollingOperation<PollingManager> {

    /**
     * Creates a new instance using the provided connection
     */
    protected PollingOperation(PollingOperationConnection connection) {
        super(connection);
    }

    /**
     * Starts the polling operation. If shared state is
     * required, the provided manager can be used to share state.
     * 
     * @param manager the associated manager
     */
    @Override
    protected void doStart(PollingManager manager) {
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
    public PollingOperationConnection getConnection() {
        return (PollingOperationConnection) super.getConnection();
    }

}
