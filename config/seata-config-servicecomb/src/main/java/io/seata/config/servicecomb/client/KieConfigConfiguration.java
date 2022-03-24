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

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author zhaozhongwei22@163.com
 */
public class KieConfigConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieConfigConfiguration.class);

    private Properties properties;

    public KieConfigConfiguration(Properties properties) {
        this.properties = properties;
    }

    public KieAddressManager createKieAddressManager() {
        String address = properties.getProperty(CommonConfiguration.KEY_CONFIG_ADDRESS, CommonConfiguration.DEFAULT_CONFIG_URL);
        if (StringUtils.isEmpty(address)) {
            return null;
        }
        KieAddressManager kieAddressManager = new KieAddressManager(Arrays.asList(address.split(CommonConfiguration.COMMA)));
        LOGGER.info("Using kie, address={}", address);
        return kieAddressManager;
    }

    public KieConfiguration createKieConfiguration() {
        KieConfiguration kieConfiguration = new KieConfiguration();
        kieConfiguration.setAppName(properties.getProperty(CommonConfiguration.KEY_SERVICE_APPLICATION, CommonConfiguration.DEFAULT));
        kieConfiguration
            .setServiceName(properties.getProperty(CommonConfiguration.KEY_SERVICE_NAME, CommonConfiguration.DEFAULT));
        kieConfiguration.setEnvironment(properties.getProperty(CommonConfiguration.KEY_SERVICE_ENVIRONMENT, CommonConfiguration.EMPTY));
        kieConfiguration.setProject(properties.getProperty(CommonConfiguration.KEY_SERVICE_PROJECT, CommonConfiguration.DEFAULT));
        kieConfiguration
            .setCustomLabel(properties.getProperty(CommonConfiguration.KEY_SERVICE_KIE_CUSTOMLABEL, CommonConfiguration.PUBLIC));
        kieConfiguration
            .setCustomLabelValue(properties.getProperty(CommonConfiguration.KEY_SERVICE_KIE_CUSTOMLABELVALUE, CommonConfiguration.EMPTY));
        kieConfiguration.setEnableCustomConfig(Boolean
            .parseBoolean(properties.getProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLECUSTOMCONFIG, CommonConfiguration.TRUE)));
        kieConfiguration.setEnableServiceConfig(Boolean
            .parseBoolean(properties.getProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLESERVICECONFIG, CommonConfiguration.TRUE)));
        kieConfiguration.setEnableAppConfig(
            Boolean.parseBoolean(properties.getProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLEAPPCONFIG, CommonConfiguration.TRUE)));
        kieConfiguration.setFirstPullRequired(Boolean
            .parseBoolean(properties.getProperty(CommonConfiguration.KEY_SERVICE_KIE_FRISTPULLREQUIRED, CommonConfiguration.TRUE)));
        kieConfiguration.setEnableLongPolling(
            Boolean.parseBoolean(properties.getProperty(CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING, CommonConfiguration.TRUE)));
        kieConfiguration.setPollingWaitInSeconds(
            Integer.parseInt(properties.getProperty(CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC, CommonConfiguration.DEFAULT_SERVICE_POLLINGWAITSEC)));
        return kieConfiguration;
    }
}
