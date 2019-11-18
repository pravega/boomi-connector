package com.boomi.connector.pravega;

import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;

import java.util.ArrayList;
import java.util.List;

public class PravegaTestConnector extends PravegaConnector implements AutoCloseable {
    private List<AutoCloseable> createdResources = new ArrayList<>();

    @Override
    protected Operation createQueryOperation(OperationContext context) {
        Operation operation = super.createQueryOperation(context);
        createdResources.add((AutoCloseable) operation);
        return operation;
    }

    @Override
    protected Operation createCreateOperation(OperationContext context) {
        Operation operation = super.createCreateOperation(context);
        createdResources.add((AutoCloseable) operation);
        return operation;
    }

    @Override
    public void close() throws Exception {
        for (AutoCloseable closeable : createdResources) {
            closeable.close();
        }
    }
}
