package com.boomi.connector.pravega;

import com.boomi.connector.api.OperationContext;

import java.util.Map;

public class WriterConfig {
    public static WriterConfig fromContext(OperationContext context) {
        WriterConfig writerConfig = new WriterConfig();
        Map<String, Object> props = context.getOperationProperties();
        writerConfig.setRoutingKeyNeeded((boolean) props.getOrDefault(Constants.ROUTINGKEY_NEEDED_PROPERTY, false));
        writerConfig.setFixedRoutingKey((String) props.get(Constants.FIXED_ROUTINGKEY_PROPERTY));
        writerConfig.setRoutingKeyConfigValue((String) props.get(Constants.ROUTINGKEY_CONFIG_VALUE_PROPERTY));
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
