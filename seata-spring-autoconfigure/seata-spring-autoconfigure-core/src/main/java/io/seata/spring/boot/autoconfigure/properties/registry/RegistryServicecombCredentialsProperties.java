package io.seata.spring.boot.autoconfigure.properties.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static io.seata.spring.boot.autoconfigure.StarterConstants.CONFIG_SERVICECOMB_CREDENTIALS_PREFIX;

@Component
@ConfigurationProperties(prefix = CONFIG_SERVICECOMB_CREDENTIALS_PREFIX)
public class RegistryServicecombCredentialsProperties {
    private String enabled;
    private String accessKey;
    private String secretKey;
    private String cipher;
    private String project;

    public String getEnabled() {
        return enabled;
    }

    public RegistryServicecombCredentialsProperties setEnabled(String enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public RegistryServicecombCredentialsProperties setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public RegistryServicecombCredentialsProperties setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getCipher() {
        return cipher;
    }

    public RegistryServicecombCredentialsProperties setCipher(String cipher) {
        this.cipher = cipher;
        return this;
    }

    public String getProject() {
        return project;
    }

    public RegistryServicecombCredentialsProperties setProject(String project) {
        this.project = project;
        return this;
    }
}