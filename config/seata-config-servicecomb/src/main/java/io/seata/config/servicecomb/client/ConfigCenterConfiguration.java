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
import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author zhaozhongwei22@163.com
 */
public class ConfigCenterConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterConfiguration.class);

    private Properties properties;

    public ConfigCenterConfiguration(Properties properties) {
        this.properties = properties;
    }

    public AddressManager createAddressManager() {
        String address = properties.getProperty(CommonConfiguration.KEY_CONFIG_ADDRESS, CommonConfiguration.EMPTY);
        if (StringUtils.isEmpty(address)) {
            return null;
        }
        String project = properties.getProperty(CommonConfiguration.KEY_SERVICE_PROJECT, CommonConfiguration.DEFAULT);
        LOGGER.info("Using config center, address={}", address);
        return new AddressManager(project, Arrays.asList(address.split(CommonConfiguration.COMMA)));
    }

    public QueryConfigurationsRequest createQueryConfigurationsRequest() {
        QueryConfigurationsRequest request = new QueryConfigurationsRequest();
        request.setApplication(properties.getProperty(CommonConfiguration.KEY_SERVICE_APPLICATION, CommonConfiguration.DEFAULT));
        request.setServiceName(properties.getProperty(CommonConfiguration.KEY_SERVICE_NAME, CommonConfiguration.DEFAULT));
        request.setVersion(properties.getProperty(CommonConfiguration.KEY_SERVICE_VERSION, CommonConfiguration.DEFAULT_VERSION));
        request.setEnvironment(properties.getProperty(CommonConfiguration.KEY_SERVICE_ENVIRONMENT, CommonConfiguration.EMPTY));
        // first time revision must be null,not empty string
        request.setRevision(null);
        return request;
    }
}
