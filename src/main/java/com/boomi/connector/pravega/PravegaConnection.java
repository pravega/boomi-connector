package com.boomi.connector.pravega;

import java.net.URI;
import java.util.Map;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.util.BaseConnection;

import io.pravega.client.admin.StreamManager;


public class PravegaConnection extends BaseConnection {

	public PravegaConnection(BrowseContext context) {
		super(context);
        Map<String, Object> connProps = this.getContext().getConnectionProperties();
        URI controllerURI = URI.create((String)connProps.get(Constants.URI_PROPERTY));
        String scope = (String)connProps.get(Constants.SCOPE_PROPERTY);
              
        StreamManager streamManager = StreamManager.create(controllerURI);
        final boolean scopeIsNew = streamManager.createScope(scope);

	}	
}