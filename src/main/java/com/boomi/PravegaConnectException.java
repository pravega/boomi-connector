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
/**
 * 
 * @author kritika.b.verma
 *
 */
public class PravegaConnectException extends Exception {
	/**
	 * 
	 * @param message
	 */
	public PravegaConnectException(String message)	{
		super(message);	
	}
/**
 * 
 * @param string
 * @param ex
 */
	public PravegaConnectException(String string, Exception ex) {
		super(string, ex);
	}
/**
 * 
 * @param ex
 */
	public PravegaConnectException(Exception ex) {
		super(ex);
	}

}
