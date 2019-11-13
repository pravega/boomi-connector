package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;

import java.util.Map;

class WriterConfig {
    static WriterConfig fromContext(OperationContext context) {
        WriterConfig writerConfig = new WriterConfig();
        Map<String, Object> props = context.getOperationProperties();
        String routingKeyType = (String) props.get(Constants.ROUTING_KEY_TYPE_PROPERTY);
        if (routingKeyType != null) writerConfig.setRoutingKeyType(RoutingKeyType.valueOf(routingKeyType));
        writerConfig.setRoutingKey((String) props.get(Constants.ROUTING_KEY_PROPERTY));
        return writerConfig;
    }

    private RoutingKeyType routingKeyType;
    private String routingKey;

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
