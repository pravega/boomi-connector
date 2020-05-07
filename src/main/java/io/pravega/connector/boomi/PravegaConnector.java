package io.pravega.connector.boomi;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.ConnectorContext;
import com.boomi.connector.api.OperationContext;
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

    /*@Override
    public UnmanagedListenOperation createListenOperation(OperationContext context) {
        return new PravegaListenOperation(context);
    }*/

    @Override
    public ListenOperation<PollingManager> createListenOperation(OperationContext context) {
        return new PollingOperation(new PollingOperationConnection(context));
    }

    @Override
    public PollingManager createListenManager(ConnectorContext context) {
        return new PollingManager(new PollingManagerConnection(context));
    }

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new PravegaBrowser(context);
    }
}
