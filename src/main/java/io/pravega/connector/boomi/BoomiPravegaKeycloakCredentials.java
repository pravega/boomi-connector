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
    private static BoomiPravegaKeycloakCredentials instance;

    private transient KeycloakAuthzClient kc = null;
    private String jsonData;

    private BoomiPravegaKeycloakCredentials(String jsonData) {
        this.jsonData = jsonData;
        init();
    }

    public static BoomiPravegaKeycloakCredentials getInstance(String json)
    {
        if (instance == null) {
            synchronized (BoomiPravegaKeycloakCredentials.class) {
                if(instance == null) {
                    instance = new BoomiPravegaKeycloakCredentials(json);
                }
            }
        }
        return instance;
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
            //file.deleteOnExit();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(jsonData);
            bw.close();
            logger.log(Level.INFO, "FILE CREATED " + jsonData);
            return file.getAbsolutePath();
        } catch (Exception E) {
            logger.log(Level.INFO, "FILE WRITING PROBLEM " + jsonData, E);
            return null;
        }
    }

    private synchronized void  init() {
        if (kc == null) {
            kc = KeycloakAuthzClient.builder().withConfigFile(createFile()).build();
        }
    }
}