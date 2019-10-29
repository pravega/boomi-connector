package com.boomi.connector.pravega;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.ConnectorException;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class PravegaConfig {
    public static PravegaConfig fromContext(BrowseContext context) {
        PravegaConfig pravegaConfig = new PravegaConfig();
        Map<String, Object> props = context.getConnectionProperties();

        // URI, scope, and stream should always be set
        String controllerUri = (String) props.get(Constants.URI_PROPERTY);
        if (controllerUri == null || controllerUri.trim().length() == 0)
            throw new ConnectorException("Pravega Controller URI must be set");
        String scope = (String) props.get(Constants.SCOPE_PROPERTY);
        if (scope == null || scope.trim().length() == 0)
            throw new ConnectorException("Pravega Scope must be set");
        String stream = (String) props.get(Constants.NAME_PROPERTY);
        if (stream == null || stream.trim().length() == 0)
            throw new ConnectorException("Pravega Stream URI must be set");

        pravegaConfig.setControllerUri(URI.create(controllerUri));
        pravegaConfig.setScope(scope);
        pravegaConfig.setStreamName(stream);
        pravegaConfig.setPravegaStandalone((boolean) props.getOrDefault(Constants.IS_PRAVEGA_STANDALONE_PROPERTY, true));
        pravegaConfig.setEnableAuth((boolean) props.getOrDefault(Constants.ENABLE_AUTH_PROPERTY, false));
        pravegaConfig.setUserName((String) props.get(Constants.USER_NAME_PROPERTY));
        pravegaConfig.setPassword((String) props.get(Constants.PASSWORD_PROPERTY));
        return pravegaConfig;
    }

    private URI controllerUri;
    private String scope;
    private String streamName;
    private boolean enableAuth;
    private String userName;
    private String password;
    private boolean isPravegaStandalone;

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

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public boolean isEnableAuth() {
        return enableAuth;
    }

    public void setEnableAuth(boolean enableAuth) {
        this.enableAuth = enableAuth;
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

    public boolean isPravegaStandalone() {
        return isPravegaStandalone;
    }

    public void setPravegaStandalone(boolean pravegaStandalone) {
        isPravegaStandalone = pravegaStandalone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PravegaConfig that = (PravegaConfig) o;
        return Objects.equals(controllerUri, that.controllerUri) &&
                Objects.equals(scope, that.scope) &&
                Objects.equals(streamName, that.streamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controllerUri, scope, streamName);
    }
}