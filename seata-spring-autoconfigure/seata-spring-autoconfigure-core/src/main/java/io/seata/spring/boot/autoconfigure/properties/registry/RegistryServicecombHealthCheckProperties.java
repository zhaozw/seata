package io.seata.spring.boot.autoconfigure.properties.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static io.seata.spring.boot.autoconfigure.StarterConstants.CONFIG_SERVICECOMB_HEALTHCHECK_PREFIX;

@Component
@ConfigurationProperties(prefix = CONFIG_SERVICECOMB_HEALTHCHECK_PREFIX)
public class RegistryServicecombHealthCheckProperties {
    private int interval;
    private int times;

    public int getInterval() {
        return interval;
    }

    public RegistryServicecombHealthCheckProperties setInterval(int interval) {
        this.interval = interval;
        return this;
    }

    public int getTimes() {
        return times;
    }

    public RegistryServicecombHealthCheckProperties setTimes(int times) {
        this.times = times;
        return this;
    }
}