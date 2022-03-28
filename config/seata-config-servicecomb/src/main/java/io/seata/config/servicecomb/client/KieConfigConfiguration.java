/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.seata.config.servicecomb.client;

import io.seata.config.Configuration;
import io.seata.config.servicecomb.SeataServicecombKeys;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author zhaozhongwei22@163.com
 */
public class KieConfigConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieConfigConfiguration.class);

    private Configuration properties;

    public KieConfigConfiguration(Configuration properties) {
        this.properties = properties;
    }

    public KieAddressManager createKieAddressManager() {
        String address =
            properties.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESS, SeataServicecombKeys.DEFAULT_CONFIG_URL);
        if (StringUtils.isEmpty(address)) {
            return null;
        }
        KieAddressManager kieAddressManager =
            new KieAddressManager(Arrays.asList(address.split(SeataServicecombKeys.COMMA)));
        LOGGER.info("Using kie, address={}", address);
        return kieAddressManager;
    }

    public KieConfiguration createKieConfiguration() {
        KieConfiguration kieConfiguration = new KieConfiguration();
        kieConfiguration.setAppName(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_APPLICATION, SeataServicecombKeys.DEFAULT));
        kieConfiguration
            .setServiceName(properties.getConfig(SeataServicecombKeys.KEY_SERVICE_NAME, SeataServicecombKeys.DEFAULT));
        kieConfiguration.setEnvironment(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_ENVIRONMENT, SeataServicecombKeys.EMPTY));
        kieConfiguration
            .setProject(properties.getConfig(SeataServicecombKeys.KEY_SERVICE_PROJECT, SeataServicecombKeys.DEFAULT));
        kieConfiguration.setCustomLabel(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_CUSTOMLABEL, SeataServicecombKeys.PUBLIC));
        kieConfiguration.setCustomLabelValue(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_CUSTOMLABELVALUE, SeataServicecombKeys.EMPTY));
        kieConfiguration.setEnableCustomConfig(Boolean.parseBoolean(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLECUSTOMCONFIG, SeataServicecombKeys.TRUE)));
        kieConfiguration.setEnableServiceConfig(Boolean.parseBoolean(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLESERVICECONFIG, SeataServicecombKeys.TRUE)));
        kieConfiguration.setEnableAppConfig(Boolean.parseBoolean(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLEAPPCONFIG, SeataServicecombKeys.TRUE)));
        kieConfiguration.setFirstPullRequired(Boolean.parseBoolean(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_FRISTPULLREQUIRED, SeataServicecombKeys.TRUE)));
        kieConfiguration.setEnableLongPolling(Boolean.parseBoolean(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_ENABLELONGPOLLING, SeataServicecombKeys.TRUE)));
        kieConfiguration.setPollingWaitInSeconds(Integer.parseInt(properties.getConfig(
            SeataServicecombKeys.KEY_SERVICE_POLLINGWAITSEC, SeataServicecombKeys.DEFAULT_SERVICE_POLLINGWAITSEC)));
        return kieConfiguration;
    }
}
