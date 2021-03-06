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

import com.boomi.connector.api.BrowseContext;

import java.util.Map;

class WriterConfig extends PravegaConfig {
    private RoutingKeyType routingKeyType;
    private String routingKey;

    public WriterConfig() {
    }

    public WriterConfig(BrowseContext context, String keycloakJSONPath) {
        super(context, keycloakJSONPath);
        Map<String, Object> props = context.getOperationProperties();
        String routingKeyType = (String) props.get(Constants.ROUTING_KEY_TYPE_PROPERTY);
        if (routingKeyType != null) setRoutingKeyType(RoutingKeyType.valueOf(routingKeyType));
        setRoutingKey((String) props.get(Constants.ROUTING_KEY_PROPERTY));
    }

    public RoutingKeyType getRoutingKeyType() {
        return routingKeyType;
    }

    public void setRoutingKeyType(RoutingKeyType routingKeyType) {
        this.routingKeyType = routingKeyType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    enum RoutingKeyType {Fixed, JsonReference}
}
