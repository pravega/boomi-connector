package io.pravega.connector.boomi;

import com.boomi.connector.api.*;
import com.boomi.connector.api.listen.ListenOperation;
import com.boomi.connector.util.listen.BaseListenConnector;

public class PravegaConnector extends BaseListenConnector {
    @Override
    protected Operation createQueryOperation(OperationContext context) {
        return new PravegaReadOperation(context);
    }

    @Override
    protected Operation createCreateOperation(OperationContext context) {
        return new PravegaWriteOperation(context);
    }

    @Override
    public ListenOperation<PravegaPollingManager> createListenOperation(OperationContext context) {
        return new PravegaPollingOperation(new PravegaPollingOperationConnection(context));
    }

    @Override
    public PravegaPollingManager createListenManager(ConnectorContext context) {
        return new PravegaPollingManager(new PravegaPollingManagerConnection(context));
    }

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new PravegaBrowser(context);
    }
}
