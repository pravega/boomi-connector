/*
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package com.boomi.connector.pravega;

public class Constants {
    static final String CONTROLLER_URI_PROPERTY = "controllerUri";
    static final String SCOPE_PROPERTY = "scope";
    static final String STREAM_PROPERTY = "stream";

    static final String CREATE_SCOPE_PROPERTY = "createScope";
    static final String ENABLE_AUTH_PROPERTY = "enableAuth";
    static final String USER_NAME_PROPERTY = "userName";
    static final String PASSWORD_PROPERTY = "password";

    static final String READER_GROUP_PROPERTY = "readerGroup";
    static final String READ_TIMEOUT_PROPERTY = "readTimeout";
    static final String ROUTING_KEY_TYPE_PROPERTY = "routingKeyType";
    static final String ROUTING_KEY_PROPERTY = "routingKey";
}
