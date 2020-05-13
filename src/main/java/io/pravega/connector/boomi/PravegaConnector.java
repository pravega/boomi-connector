package io.pravega.connector.boomi;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.ConnectorContext;
import com.boomi.connector.api.OperationContext;
import java.util.WeakHashMap;
import com.boomi.connector.api.*;
import com.boomi.connector.api.listen.ListenOperation;
import com.boomi.connector.util.listen.BaseListenConnector;

public class PravegaConnector extends BaseListenConnector {

    private WeakHashMap<String, String> map = new WeakHashMap<>();

    @Override
    protected Operation createQueryOperation(OperationContext context) {
        return new PravegaReadOperation(context, PravegaUtil.checkAndSetCredentials(context, map));
    }

    @Override
    protected Operation createCreateOperation(OperationContext context) {
        return new PravegaWriteOperation(context, PravegaUtil.checkAndSetCredentials(context, map));
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
