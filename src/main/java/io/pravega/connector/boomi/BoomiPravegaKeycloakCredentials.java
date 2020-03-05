package io.pravega.connector.boomi;

import io.pravega.client.stream.impl.Credentials;
import io.pravega.keycloak.client.KeycloakAuthzClient;

import static io.pravega.auth.AuthConstants.BEARER;

public class BoomiPravegaKeycloakCredentials implements Credentials {

    private transient KeycloakAuthzClient kc = null;
    private String jsonDataPath;

    public BoomiPravegaKeycloakCredentials(String path) {
        this.jsonDataPath = path;
        init();
    }

    @Override
    public String getAuthenticationType() {
        return BEARER;
    }

    @Override
    public String getAuthenticationToken() {
        init();
        return kc.getRPT();
    }

    private synchronized void init() {
        if (kc == null) {
            kc = KeycloakAuthzClient.builder().withConfigFile(this.jsonDataPath).build();
        }
    }
}