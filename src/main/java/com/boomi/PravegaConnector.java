package com.boomi;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;
import com.boomi.connector.util.BaseConnector;

public class PravegaConnector extends BaseConnector {

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new PravegaBrowser(createConnection(context));
    }    

    @Override
    protected Operation createGetOperation(OperationContext context) {
        return new PravegaGetOperation(createConnection(context));
    }

    @Override
    protected Operation createCreateOperation(OperationContext context) {
        return new PravegaCreateOperation(createConnection(context));
    }
   
    private PravegaConnection createConnection(BrowseContext context) {
        return new PravegaConnection(context);
    }
}