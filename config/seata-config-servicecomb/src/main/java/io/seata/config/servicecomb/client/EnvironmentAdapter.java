package io.seata.config.servicecomb.client;

import io.seata.config.Configuration;
/*
 * 这里不需要引入这个接口，传Properties应该好一些
 */
public interface EnvironmentAdapter {
    default public String getProperty(String key, String defaultValue) {
        return defaultValue;
    }
}
