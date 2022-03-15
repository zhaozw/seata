package io.seata.config.servicecomb.client;

import io.seata.config.servicecomb.client.auth.AuthHeaderProviders;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ServicecombConfigurationHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombConfigurationHelper.class);

    volatile ConfigCenterManager configCenterManager;

    volatile KieConfigManager kieConfigManager;

    HttpTransport httpTransport;

    private boolean isKie = false;

    private ConfigCenterConfiguration configCenterConfiguration;

    private KieConfigConfiguration kieConfigConfiguration;

    private KieConfiguration kieConfiguration;

    private ConfigConverter configConverter;

    private Properties properties;

    public ServicecombConfigurationHelper(Properties properties){


        this.properties = properties;
        configConverter = initConfigConverter();

        configCenterConfiguration = new ConfigCenterConfiguration(properties);
        kieConfigConfiguration = new KieConfigConfiguration(properties);

        addConfigCenterProperties(properties);

    }

    /***/
     private ConfigConverter initConfigConverter() {
         String fileSources = properties.getProperty(CommonConfiguration.KEY_CONFIG_FILESOURCE, "");
         if (StringUtils.isEmpty(fileSources)) {
            configConverter = new ConfigConverter(null);
         } else {
            configConverter = new ConfigConverter(Arrays.asList(fileSources.split("，")));
         }
         return configConverter;
     }

    private void addConfigCenterProperties(Properties properties) {
        isKie = properties.getProperty(CommonConfiguration.KEY_CONFIG_ADDRESSTYPE, "kie").equals("kie");
        RequestConfig.Builder config = HttpTransportFactory.defaultRequestConfig();

        this.setTimeOut(config);

        httpTransport = HttpTransportFactory
                .createHttpTransport(AuthHeaderProviders.createSSLProperties(properties),
                        AuthHeaderProviders.getRequestAuthHeaderProvider(properties), config.build());

        //判断是否使用KIE作为配置中心
        if (isKie) {
            configKieClient(properties);
        } else {
            configCenterClient(properties);
        }
    }


    private void configCenterClient(Properties properties) {
        QueryConfigurationsRequest queryConfigurationsRequest = configCenterConfiguration.createQueryConfigurationsRequest();
        AddressManager addressManager = configCenterConfiguration.createAddressManager();
        if (addressManager == null) {
            LOGGER.warn("Config center address is not configured and will not enable dynamic config.");
            return;
        }
        ConfigCenterClient configCenterClient = new ConfigCenterClient(addressManager, httpTransport);
        try {
            QueryConfigurationsResponse response = configCenterClient.queryConfigurations(queryConfigurationsRequest);
            if (response.isChanged()) {
                configConverter.updateData(response.getConfigurations());
            }
            queryConfigurationsRequest.setRevision(response.getRevision());

        } catch (Exception e) {
            LOGGER.warn("set up Servicesomb configuration failed at startup.", e);
        }
        configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus(), configConverter);
        configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
        configCenterManager.startConfigCenterManager();
    }

    //use KIE as config center
    private void configKieClient(Properties properties) {

        kieConfiguration = kieConfigConfiguration.createKieConfiguration();

        KieAddressManager kieAddressManager = kieConfigConfiguration.createKieAddressManager();
        if (kieAddressManager == null) {
            LOGGER.warn("Kie address is not configured and will not enable dynamic config.");
            return;
        }

        KieClient kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);

        kieConfigManager = new KieConfigManager(kieClient, EventManager.getEventBus(),
                kieConfiguration, configConverter);
        kieConfigManager.firstPull();
        kieConfigManager.startConfigKieManager();
    }

    private void setTimeOut(RequestConfig.Builder config) {
        if (!isKie) {
            return;
        }
        String test = properties.getProperty(CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING,
                "true");
        if (Boolean.parseBoolean(test)) {
            int pollingWaitInSeconds = Integer.valueOf(properties.getProperty(CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC,
                    "30"));
            config.setSocketTimeout(pollingWaitInSeconds * 1000 + 5000);
        }
    }

    public Map<String, Object> getCurrentData() {
        return configConverter.getCurrentData();
    }

}
