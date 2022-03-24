/*

  * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

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

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import static io.seata.config.servicecomb.client.CommonConfiguration.DEFAULT_SERVICE_POLLINGWAITSEC;
import static io.seata.config.servicecomb.client.CommonConfiguration.TRUE;


/**
 * @author zhaozhongwei22@163.com
 */
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

    public ServicecombConfigurationHelper(Properties properties) {

        this.properties = properties;
        configConverter = initConfigConverter();

        configCenterConfiguration = new ConfigCenterConfiguration(properties);
        kieConfigConfiguration = new KieConfigConfiguration(properties);

        addConfigCenterProperties(properties);

    }

    /***/
    private ConfigConverter initConfigConverter() {
        String fileSources = properties.getProperty(CommonConfiguration.KEY_CONFIG_FILESOURCE, CommonConfiguration.EMPTY);
        if (StringUtils.isEmpty(fileSources)) {
            configConverter = new ConfigConverter(null);
        } else {
            configConverter = new ConfigConverter(Arrays.asList(fileSources.split(CommonConfiguration.COMMA)));
        }
        return configConverter;
    }

    private void addConfigCenterProperties(Properties properties) {
        isKie = CommonConfiguration.KIE.equals(properties.getProperty(CommonConfiguration.KEY_CONFIG_ADDRESSTYPE, CommonConfiguration.KIE));
        RequestConfig.Builder config = HttpTransportFactory.defaultRequestConfig();

        this.setTimeOut(config);

        httpTransport = HttpTransportFactory.createHttpTransport(AuthHeaderProviders.createSslProperties(properties),
            AuthHeaderProviders.getRequestAuthHeaderProvider(properties), config.build());

        if (isKie) {
            configKieClient(properties);
        } else {
            configCenterClient(properties);
        }
    }

    private void configCenterClient(Properties properties) {
        QueryConfigurationsRequest queryConfigurationsRequest =
            configCenterConfiguration.createQueryConfigurationsRequest();
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

    private void configKieClient(Properties properties) {

        kieConfiguration = kieConfigConfiguration.createKieConfiguration();

        KieAddressManager kieAddressManager = kieConfigConfiguration.createKieAddressManager();
        if (kieAddressManager == null) {
            LOGGER.warn("Kie address is not configured and will not enable dynamic config.");
            return;
        }

        KieClient kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);

        kieConfigManager =
            new KieConfigManager(kieClient, EventManager.getEventBus(), kieConfiguration, configConverter);
        kieConfigManager.firstPull();
        kieConfigManager.startConfigKieManager();
    }

    private void setTimeOut(RequestConfig.Builder config) {
        if (!isKie) {
            return;
        }
        String test = properties.getProperty(CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING, TRUE);
        if (Boolean.parseBoolean(test)) {
            int pollingWaitInSeconds =
                Integer.valueOf(properties.getProperty(CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC, DEFAULT_SERVICE_POLLINGWAITSEC));
            config.setSocketTimeout(pollingWaitInSeconds * 1000 + 5000);
        }
    }

    public Map<String, Object> getCurrentData() {
        return configConverter.getCurrentData();
    }

}
