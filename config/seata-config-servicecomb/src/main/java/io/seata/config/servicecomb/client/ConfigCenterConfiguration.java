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
import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author zhaozhongwei22@163.com
 */
public class ConfigCenterConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterConfiguration.class);

    private Configuration properties;

    public ConfigCenterConfiguration(Configuration properties) {
        this.properties = properties;
    }

    public AddressManager createAddressManager() {
        String address = properties.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESS, SeataServicecombKeys.EMPTY);
        if (StringUtils.isEmpty(address)) {
            return null;
        }
        String project = properties.getConfig(SeataServicecombKeys.KEY_SERVICE_PROJECT, SeataServicecombKeys.DEFAULT);
        LOGGER.info("Using config center, address={}", address);
        return new AddressManager(project, Arrays.asList(address.split(SeataServicecombKeys.COMMA)));
    }

    public QueryConfigurationsRequest createQueryConfigurationsRequest() {
        QueryConfigurationsRequest request = new QueryConfigurationsRequest();
        request.setApplication(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_APPLICATION, SeataServicecombKeys.DEFAULT));
        request
            .setServiceName(properties.getConfig(SeataServicecombKeys.KEY_SERVICE_NAME, SeataServicecombKeys.DEFAULT));
        request.setVersion(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_VERSION, SeataServicecombKeys.DEFAULT_VERSION));
        request.setEnvironment(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_ENVIRONMENT, SeataServicecombKeys.EMPTY));
        // first time revision must be null,not empty string
        request.setRevision(null);
        return request;
    }
}
