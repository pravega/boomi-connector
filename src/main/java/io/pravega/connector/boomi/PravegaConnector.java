package io.pravega.connector.boomi;

import com.boomi.connector.api.*;
import com.boomi.connector.api.listen.ListenOperation;
import com.boomi.connector.util.listen.BaseListenConnector;

import java.util.WeakHashMap;

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
        return new PravegaPollingOperation(new PravegaPollingOperationConnection(context, PravegaUtil.checkAndSetCredentials(context, map)));
    }

    @Override
    public PravegaPollingManager createListenManager(ConnectorContext context) {
        return new PravegaPollingManager(new PravegaPollingManagerConnection(context, PravegaUtil.checkAndSetCredentials(context, map)));
    }

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new PravegaBrowser(context);
    }

}
