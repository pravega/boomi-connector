package com.boomi;

public class PravegaConnectException extends Exception {
	
	public PravegaConnectException(String message)	{
		super(message);	
	}

	public PravegaConnectException(String string, Exception ex) {
		super(string, ex);
	}

	public PravegaConnectException(Exception ex) {
		super(ex);
	}

}
