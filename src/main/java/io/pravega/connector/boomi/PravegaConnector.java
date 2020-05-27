package io.pravega.connector.boomi;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;
import com.boomi.connector.util.listen.UnmanagedListenConnector;
import com.boomi.connector.util.listen.UnmanagedListenOperation;

import java.util.WeakHashMap;

public class PravegaConnector extends UnmanagedListenConnector {

    //store keycloak.json contents file as key and file path as value
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
    public UnmanagedListenOperation createListenOperation(OperationContext context) {
        return new PravegaListenOperation(context, PravegaUtil.checkAndSetCredentials(context, map));
    }

    @Override
    public Browser createBrowser(BrowseContext context) {
        return new PravegaBrowser(context);
    }

}
