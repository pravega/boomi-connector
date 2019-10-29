package com.boomi.connector.pravega;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;
import com.boomi.connector.util.BaseConnector;

import java.util.ArrayList;
import java.util.List;

public class PravegaConnector extends BaseConnector implements AutoCloseable {
    private List<PravegaConnection> createdConnections = new ArrayList<>();

    @Override
    protected Operation createGetOperation(OperationContext context) {
        return new PravegaGetOperation(createConnection(context));
    }

    @Override
    protected Operation createCreateOperation(OperationContext context) {
        return new PravegaCreateOperation(createConnection(context));
    }
   
    private PravegaConnection createConnection(BrowseContext context) {
        PravegaConnection connection = new PravegaConnection(context);
        createdConnections.add(connection);
        return connection;
    }

	@Override
	public Browser createBrowser(BrowseContext context) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public void close() {
        for (PravegaConnection connection : createdConnections) {
            connection.close();
        }
    }
}
