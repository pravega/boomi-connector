package io.pravega.connector.boomi;

import com.boomi.connector.api.BrowseContext;
import com.boomi.connector.api.ConnectorException;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class PravegaConfig {
    private URI controllerUri;
    private String scope;
    private String stream;
    private String authMethod;
    private String userName;
    private String password;
    private String keycloakJSONPath;
    private boolean createScope;

    public PravegaConfig() {
    }

    public PravegaConfig(BrowseContext context, String keycloakJsonPath) {
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
        setAuth((String) getOrDefault(props, Constants.AUTH_TYPE_PROPERTY, Constants.AUTH_TYPE_PROPERTY_NONE));
        setUserName((String) props.get(Constants.USER_NAME_PROPERTY));
        setPassword((String) props.get(Constants.PASSWORD_PROPERTY));
        setKeycloakJSONPath(keycloakJsonPath);
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

    public String getAuth() {
        return authMethod;
    }

    public void setAuth(String auth) {
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

    public String getKeycloakJSONPath() {
        return keycloakJSONPath;
    }

    public void setKeycloakJSONPath(String keycloakJSONPath) {
        this.keycloakJSONPath = keycloakJSONPath;
    }

    public boolean isCreateScope() {
        return createScope;
    }

    public void setCreateScope(boolean createScope) {
        this.createScope = createScope;
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
}
