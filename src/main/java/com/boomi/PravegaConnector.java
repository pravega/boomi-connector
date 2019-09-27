/*
 * Copyright (c) 2017 Dell Inc. and Accenture, or its subsidiaries. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 */

package com.boomi;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.Browser;
import com.boomi.connector.api.Operation;
import com.boomi.connector.api.OperationContext;
import com.boomi.connector.util.BaseConnector;

/**
 * 
 * @author kritika.b.verma
 *
 */
public class PravegaConnector extends BaseConnector {
	/**
	 * 
	 */
	@Override
	public Browser createBrowser(BrowseContext context) {
		return new PravegaBrowser(createConnection(context));
	}

	/**
	 * 
	 */
	@Override
	protected Operation createGetOperation(OperationContext context) {
		return new PravegaGetOperation(createConnection(context));
	}

	/**
	 * 
	 */
	@Override
	protected Operation createCreateOperation(OperationContext context) {
		return new PravegaCreateOperation(createConnection(context));
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	private PravegaConnection createConnection(BrowseContext context) {
		return new PravegaConnection(context);
	}
}