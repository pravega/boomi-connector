package io.pravega.connector.boomi;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;
import com.boomi.connector.util.BaseConnector;
import com.boomi.connector.util.listen.UnmanagedListenConnector;
import com.boomi.connector.util.listen.UnmanagedListenOperation;

public class PravegaConnector extends UnmanagedListenConnector {
    @Override
    protected Operation createQueryOperation(OperationContext context) {
        return new PravegaReadOperation(context);
    }

    @Override
    protected Operation createCreateOperation(OperationContext context) {
        return new PravegaWriteOperation(context);
    }

    @Override
    public UnmanagedListenOperation createListenOperation(OperationContext context) {
        return new PravegaListenOperation(context);
    }

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new PravegaBrowser(context);
    }
}
