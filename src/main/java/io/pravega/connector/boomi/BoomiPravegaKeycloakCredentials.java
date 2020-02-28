package io.pravega.connector.boomi;

import io.pravega.client.stream.impl.Credentials;
import io.pravega.keycloak.client.KeycloakAuthzClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.pravega.auth.AuthConstants.BEARER;

public class BoomiPravegaKeycloakCredentials implements Credentials {

    private static final Logger logger = Logger.getLogger(BoomiPravegaKeycloakCredentials.class.getName());

    private transient KeycloakAuthzClient kc = null;
    private String jsonData;

    public BoomiPravegaKeycloakCredentials(String jsonData) {
        this.jsonData = jsonData;
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

    private String generateRandomFileName() {
        byte[] array = new byte[7];
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        return generatedString;
    }

    private String createFile() {
        try {
            File file = File.createTempFile(generateRandomFileName(), ".json");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(jsonData);
            bw.close();
            logger.log(Level.INFO, "FILE CREATED " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception E) {
            logger.log(Level.INFO, "FILE WRITING PROBLEM", E);
            return null;
        }
    }

    private void init() {
        if (kc == null) {
            kc = KeycloakAuthzClient.builder().withConfigFile(createFile()).build();
        }
    }
}