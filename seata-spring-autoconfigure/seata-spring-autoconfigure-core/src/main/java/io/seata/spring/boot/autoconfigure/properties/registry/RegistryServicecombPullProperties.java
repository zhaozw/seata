package io.seata.spring.boot.autoconfigure.properties.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static io.seata.spring.boot.autoconfigure.StarterConstants.CONFIG_SERVICECOMB_PULL_PREFIX;

@Component
@ConfigurationProperties(prefix = CONFIG_SERVICECOMB_PULL_PREFIX)
public class RegistryServicecombPullProperties {
    private int interval;

    public int getInterval() {
        return interval;
    }

    public RegistryServicecombPullProperties setInterval(int interval) {
        this.interval = interval;
        return this;
    }
}