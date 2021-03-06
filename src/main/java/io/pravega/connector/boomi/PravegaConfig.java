/*
 * Copyright (c) Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package io.pravega.connector.boomi;

import com.boomi.connector.api.ConnectorContext;
import com.boomi.connector.api.ConnectorException;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PravegaConfig {
    private URI controllerUri;
    private String scope;
    private String stream;
    private AuthenticationType authMethod;
    private String userName;
    private String password;
    private String keycloakJSONString;
    private boolean createScope;
    private long interval;
    private TimeUnit unit;

    public PravegaConfig() {
    }

    public PravegaConfig(ConnectorContext context, String keycloakJsonString) {
        Map<String, Object> props = context.getConnectionProperties();

        // URI, scope, and stream should always be set
        String controllerUri = (String) props.get(Constants.CONTROLLER_URI_PROPERTY);
        if (controllerUri == null || controllerUri.trim().length() == 0)
            throw new ConnectorException("Pravega Controller URI must be set");
        String scope = (String) props.get(Constants.SCOPE_PROPERTY);
        if (scope == null || scope.trim().length() == 0)
            throw new ConnectorException("Pravega Scope must be set");
        String stream = (String) props.get(Constants.STREAM_PROPERTY);
        if (stream == null || stream.trim().length() == 0)
            throw new ConnectorException("Pravega Stream URI must be set");

        setControllerUri(URI.create(controllerUri));
        setScope(scope);
        setStream(stream);
        setCreateScope((boolean) getOrDefault(props, Constants.CREATE_SCOPE_PROPERTY, true));
        String auth = (String) props.get(Constants.AUTH_TYPE_PROPERTY);
        if (auth != null)
            setAuth(AuthenticationType.valueOf(auth));
        setUserName((String) props.get(Constants.USER_NAME_PROPERTY));
        setPassword((String) props.get(Constants.PASSWORD_PROPERTY));
        setKeycloakJSONString(keycloakJsonString);
        setInterval((long) props.get(Constants.INTERVAL));
        setUnit((String) props.get(Constants.TIME_UNIT));
    }

    protected Object getOrDefault(Map<String, Object> map, String key, Object defaultValue) {
        Object value = map.get(key);
        if (value == null || (value instanceof String && ((String) value).trim().length() == 0))
            value = defaultValue;
        return value;
    }

    public URI getControllerUri() {
        return controllerUri;
    }

    public void setControllerUri(URI controllerUri) {
        this.controllerUri = controllerUri;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public AuthenticationType getAuth() {
        return authMethod;
    }

    public void setAuth(AuthenticationType auth) {
        this.authMethod = auth;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeycloakJSONString() {
        return keycloakJSONString;
    }

    public void setKeycloakJSONString(String keycloakJSONString) {
        this.keycloakJSONString = keycloakJSONString;
    }

    public boolean isCreateScope() {
        return createScope;
    }

    public void setCreateScope(boolean createScope) {
        this.createScope = createScope;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(String timeUnit) {
        this.unit = TimeUnit.valueOf(timeUnit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PravegaConfig that = (PravegaConfig) o;
        return Objects.equals(controllerUri, that.controllerUri) &&
                Objects.equals(scope, that.scope) &&
                Objects.equals(stream, that.stream);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controllerUri, scope, stream);
    }

    enum AuthenticationType {None, Basic, Keycloak}
}
