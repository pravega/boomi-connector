package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;

import java.util.Map;

class WriterConfig {
    static WriterConfig fromContext(OperationContext context) {
        WriterConfig writerConfig = new WriterConfig();
        Map<String, Object> props = context.getOperationProperties();
        writerConfig.setRoutingKeyNeeded((boolean) props.getOrDefault(Constants.ROUTING_KEY_NEEDED_PROPERTY, false));
        writerConfig.setFixedRoutingKey((String) props.get(Constants.FIXED_ROUTING_KEY_PROPERTY));
        writerConfig.setRoutingKeyConfigValue((String) props.get(Constants.ROUTING_KEY_CONFIG_VALUE_PROPERTY));
        return writerConfig;
    }

    private boolean isRoutingKeyNeeded;
    private String fixedRoutingKey;
    private String routingKeyConfigValue;

    public boolean isRoutingKeyNeeded() {
        return isRoutingKeyNeeded;
    }

    public void setRoutingKeyNeeded(boolean routingKeyNeeded) {
        isRoutingKeyNeeded = routingKeyNeeded;
    }

    public String getFixedRoutingKey() {
        return fixedRoutingKey;
    }

    public void setFixedRoutingKey(String fixedRoutingKey) {
        this.fixedRoutingKey = fixedRoutingKey;
    }

    public String getRoutingKeyConfigValue() {
        return routingKeyConfigValue;
    }

    public void setRoutingKeyConfigValue(String routingKeyConfigValue) {
        this.routingKeyConfigValue = routingKeyConfigValue;
    }
}
