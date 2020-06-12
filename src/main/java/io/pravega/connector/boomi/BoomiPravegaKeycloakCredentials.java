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

import io.pravega.client.stream.impl.Credentials;
import io.pravega.keycloak.client.KeycloakAuthzClient;

import static io.pravega.auth.AuthConstants.BEARER;

public class BoomiPravegaKeycloakCredentials implements Credentials {

    private transient KeycloakAuthzClient kc = null;
    private String keycloakJSONPath;

    public BoomiPravegaKeycloakCredentials(String path) {
        this.keycloakJSONPath = path;
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
            kc = KeycloakAuthzClient.builder().withConfigFile(this.keycloakJSONPath).build();
        }
    }
}