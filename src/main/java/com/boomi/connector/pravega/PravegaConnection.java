package com.boomi.connector.pravega;

import java.net.URI;
import java.util.Map;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.util.BaseConnection;

import io.pravega.client.admin.StreamManager;


public class PravegaConnection extends BaseConnection {

	public PravegaConnection(BrowseContext context) {
		super(context);
	}	
}