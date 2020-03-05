package io.pravega.connector.boomi;

import com.boomi.connector.api.*;
import com.boomi.connector.util.BaseBrowser;

import java.util.WeakHashMap;

public class PravegaBrowser extends BaseBrowser implements ConnectionTester {
    private WeakHashMap<String, String> map = new WeakHashMap<>();

    public PravegaBrowser(BrowseContext browseContext) {
        super(browseContext);
    }

    @Override
    public ObjectDefinitions getObjectDefinitions(java.lang.String objectTypeId, java.util.Collection<ObjectDefinitionRole> roles) {
        return null;
    }

    @Override
    public ObjectTypes getObjectTypes() {
        return null;
    }

    @Override
    public void testConnection() {
        try {
            PravegaUtil.testConnection(getContext(), PravegaUtil.checkandSetCredentials(getContext(), map));
        } catch (Throwable t) {
            throw new ConnectorException("Could not initialize connection to Pravega", t);
        }
    }

}
