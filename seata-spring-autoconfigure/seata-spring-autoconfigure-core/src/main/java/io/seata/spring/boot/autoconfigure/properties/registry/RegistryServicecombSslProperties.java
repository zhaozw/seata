package io.seata.spring.boot.autoconfigure.properties.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static io.seata.spring.boot.autoconfigure.StarterConstants.CONFIG_SERVICECOMB_SSL_PREFIX;

@Component
@ConfigurationProperties(prefix = CONFIG_SERVICECOMB_SSL_PREFIX)
public class RegistryServicecombSslProperties {
    private String enabled;
    private String ciphers;
    private String authPeer;
    private String checkCNHost;
    private String checkCNWhite;
    private String checkCNWhiteFile;
    private String allowRenegotiate;
    private String storePath;
    private String trustStore;
    private String trustStoreType;
    private String trustStoreValue;
    private String keyStore;
    private String keyStoreType;
    private String keyStoreValue;
    private String crl;
    private String sslCustomClass;

    public String getEnabled() {
        return enabled;
    }

    public RegistryServicecombSslProperties setEnabled(String enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getSslCustomClass() {
        return sslCustomClass;
    }

    public RegistryServicecombSslProperties setSslCustomClass(String sslCustomClass) {
        this.sslCustomClass = sslCustomClass;
        return this;
    }

    public String getCiphers() {
        return ciphers;
    }

    public RegistryServicecombSslProperties setCiphers(String ciphers) {
        this.ciphers = ciphers;
        return this;
    }

    public String getAuthPeer() {
        return authPeer;
    }

    public RegistryServicecombSslProperties setAuthPeer(String authPeer) {
        this.authPeer = authPeer;
        return this;
    }

    public String getCheckCNHost() {
        return checkCNHost;
    }

    public RegistryServicecombSslProperties setCheckCNHost(String checkCNHost) {
        this.checkCNHost = checkCNHost;
        return this;
    }

    public String getCheckCNWhite() {
        return checkCNWhite;
    }

    public RegistryServicecombSslProperties setCheckCNWhite(String checkCNWhite) {
        this.checkCNWhite = checkCNWhite;
        return this;
    }

    public String getCheckCNWhiteFile() {
        return checkCNWhiteFile;
    }

    public RegistryServicecombSslProperties setCheckCNWhiteFile(String checkCNWhiteFile) {
        this.checkCNWhiteFile = checkCNWhiteFile;
        return this;
    }

    public String getAllowRenegotiate() {
        return allowRenegotiate;
    }

    public RegistryServicecombSslProperties setAllowRenegotiate(String allowRenegotiate) {
        this.allowRenegotiate = allowRenegotiate;
        return this;
    }

    public String getStorePath() {
        return storePath;
    }

    public RegistryServicecombSslProperties setStorePath(String storePath) {
        this.storePath = storePath;
        return this;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public RegistryServicecombSslProperties setTrustStore(String trustStore) {
        this.trustStore = trustStore;
        return this;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public RegistryServicecombSslProperties setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
        return this;
    }

    public String getTrustStoreValue() {
        return trustStoreValue;
    }

    public RegistryServicecombSslProperties setTrustStoreValue(String trustStoreValue) {
        this.trustStoreValue = trustStoreValue;
        return this;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public RegistryServicecombSslProperties setKeyStore(String keyStore) {
        this.keyStore = keyStore;
        return this;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public RegistryServicecombSslProperties setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
        return this;
    }

    public String getKeyStoreValue() {
        return keyStoreValue;
    }

    public RegistryServicecombSslProperties setKeyStoreValue(String keyStoreValue) {
        this.keyStoreValue = keyStoreValue;
        return this;
    }

    public String getCrl() {
        return crl;
    }

    public RegistryServicecombSslProperties setCrl(String crl) {
        this.crl = crl;
        return this;
    }
}