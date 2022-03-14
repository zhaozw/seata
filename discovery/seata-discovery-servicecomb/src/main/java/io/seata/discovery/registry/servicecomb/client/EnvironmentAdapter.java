package io.seata.discovery.registry.servicecomb.client;

public interface EnvironmentAdapter {
    default public String getProperty(String key, String defaultValue) {
        return defaultValue;
    }
}
