package io.pravega.connector.boomi;

import com.boomi.connector.api.BrowseContext;

import java.util.Map;

class WriterConfig extends PravegaConfig {
    private RoutingKeyType routingKeyType;
    private String routingKey;

    public WriterConfig() {
    }

    public WriterConfig(BrowseContext context) {
        super(context);
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
