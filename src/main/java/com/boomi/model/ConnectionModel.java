package com.boomi.model;

import java.io.Serializable;
import java.net.URI;

public class ConnectionModel implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String host;
	public  String scope;
    public  String streamName;
    public  URI controllerURI;
    private String message;
    private String routingKey;
    
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getStreamName() {
		return streamName;
	}
	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}
	public URI getControllerURI() {
		return controllerURI;
	}
	public void setControllerURI(URI controllerURI) {
		this.controllerURI = controllerURI;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getRoutingKey() {
		return routingKey;
	}
	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}


}
