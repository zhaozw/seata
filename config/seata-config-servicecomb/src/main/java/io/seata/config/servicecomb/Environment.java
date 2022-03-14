package io.seata.config.servicecomb;

import io.seata.config.Configuration;
import io.seata.config.servicecomb.client.CommonConfiguration;
import io.seata.config.servicecomb.client.EnvironmentAdapter;

public class Environment implements EnvironmentAdapter {
    private Configuration fileConfig;
    public Environment(Configuration fileConfig) {
        this.fileConfig=fileConfig;
    }

    public String getProperty(String key, String defaultValue) {
        switch (key){
            case CommonConfiguration
                    .KEY_SERVICE_PROJECT:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_PROJECT,defaultValue);
            case CommonConfiguration
                    .KEY_AK_SK_ENABLED:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_AK_SK_ENABLED,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_APPLICATION:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_APPLICATION,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_NAME:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_NAME,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_VERSION:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_VERSION,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_ENVIRONMENT:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_ENVIRONMENT,defaultValue);
            case CommonConfiguration
                    .KEY_CONFIG_ADDRESS:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESS,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_KIE_CUSTOMLABEL:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_CUSTOMLABEL,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_KIE_CUSTOMLABELVALUE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_CUSTOMLABELVALUE,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_KIE_ENABLECUSTOMCONFIG:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLECUSTOMCONFIG,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_KIE_ENABLESERVICECONFIG:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLESERVICECONFIG,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_KIE_ENABLEAPPCONFIG:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLEAPPCONFIG,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_KIE_FRISTPULLREQUIRED:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_FRISTPULLREQUIRED,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_ENABLELONGPOLLING:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_ENABLELONGPOLLING,defaultValue);
            case CommonConfiguration
                    .KEY_SERVICE_POLLINGWAITSEC:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SERVICE_POLLINGWAITSEC,defaultValue);
            case CommonConfiguration
                    .KEY_CONFIG_FILESOURCE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_CONFIG_FILESOURCE,defaultValue);
            case CommonConfiguration
                    .KEY_CONFIG_ADDRESSTYPE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESSTYPE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_ENABLED:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_ENABLED,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_ENGINE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_ENGINE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_PROTOCOLS:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_PROTOCOLS,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_CIPHERS:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_CIPHERS,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_AUTH_PEER:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_AUTH_PEER,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_CHECKCN_HOST:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_HOST,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_CHECKCN_WHITE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_WHITE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_CHECKCN_WHITE_FILE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_WHITE_FILE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_ALLOW_RENEGOTIATE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_ALLOW_RENEGOTIATE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_STORE_PATH:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_STORE_PATH,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_KEYSTORE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_KEYSTORE_TYPE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE_TYPE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_KEYSTORE_VALUE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE_VALUE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_TRUST_STORE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_TRUST_STORE_TYPE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE_TYPE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_TRUST_STORE_VALUE:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE_VALUE,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_CRL:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_CRL,defaultValue);
            case CommonConfiguration
                    .KEY_SSL_SSL_CUSTOM_CLASS:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_SSL_SSL_CUSTOM_CLASS,defaultValue);
            case CommonConfiguration
                    .KEY_AK_SK_ACCESS_KEY:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_AK_SK_ACCESS_KEY,defaultValue);
            case CommonConfiguration
                    .KEY_AK_SK_SECRET_KEY:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_AK_SK_SECRET_KEY,defaultValue);
            case CommonConfiguration
                    .KEY_AK_SK_CIPHER:
                return fileConfig.getConfig(SeataServicecombKeys.KEY_AK_SK_CIPHER,defaultValue);
            default:
                return defaultValue;
        }
    }
}
