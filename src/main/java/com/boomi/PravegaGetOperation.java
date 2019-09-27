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

import com.boomi.connector.api.GetRequest;
import com.boomi.connector.api.OperationResponse;
import com.boomi.connector.util.BaseGetOperation;

/**
 * 
 * @author kritika.b.verma
 *
 */
public class PravegaGetOperation extends BaseGetOperation {

	protected PravegaGetOperation(PravegaConnection conn) {
		super(conn);
	}

	/**
	 * 
	 */

	@Override
	protected void executeGet(GetRequest request, OperationResponse response) {

	}

	/**
	 * 
	 */
	@Override
	public PravegaConnection getConnection() {
		return (PravegaConnection) super.getConnection();
	}
}