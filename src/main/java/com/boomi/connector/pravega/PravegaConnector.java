package com.boomi.connector.pravega;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;
import com.boomi.connector.util.BaseConnector;

public class PravegaConnector extends BaseConnector {
    @Override
    protected Operation createQueryOperation(OperationContext context) {
        return new PravegaQueryOperation(context);
    }

    @Override
    protected Operation createCreateOperation(OperationContext context) {
        return new PravegaCreateOperation(context);
    }

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new PravegaBrowser(context);
    }
}
