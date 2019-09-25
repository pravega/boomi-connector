/*
*  Copyright 2019 Accenture. All Rights Reserved.
*  The trademarks used in these materials are the properties of their respective owners.
*  This work is protected by copyright law and contains valuable trade secrets and
*  confidential information.
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