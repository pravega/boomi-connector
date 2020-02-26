package io.pravega.connector.boomi;

import io.pravega.client.stream.impl.Credentials;
import io.pravega.keycloak.client.KeycloakAuthzClient;

import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.pravega.auth.AuthConstants.BEARER;

public class BoomiPravegaKeycloakCredentials implements Credentials {

    private static final Logger logger = Logger.getLogger(BoomiPravegaKeycloakCredentials.class.getName());

    private transient KeycloakAuthzClient kc = null;
    private String jsonData;

    public BoomiPravegaKeycloakCredentials(String jsonData) {
        this.jsonData = jsonData;
        createFile();
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

    private void createFile() {

        try {
            PrintWriter writer = new PrintWriter("keycloak.json", "UTF-8");
            writer.println(jsonData);
            writer.close();
            logger.log(Level.INFO, "FILE CREATED " + getFilename());
        } catch (Exception E) {
            logger.log(Level.INFO, "FILE WRITING PROBLEM", E);
        }
    }

    private String getFilename() {
        File file = new File("keycloak.json");
        String path = file.getAbsolutePath();
        logger.log(Level.INFO, "FILE PATH" + path);
        return path;
    }

    private void init() {
        if (kc == null) {
            kc = KeycloakAuthzClient.builder().withConfigFile(getFilename()).build();
        }
    }
}