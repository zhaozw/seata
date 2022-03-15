/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

public class ConfigCenterConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigCenterConfiguration.class);

  private Properties properties;

  public ConfigCenterConfiguration(Properties properties) {
    this.properties = properties;
  }

  public AddressManager createAddressManager() {
    String address = properties.getProperty(CommonConfiguration.KEY_CONFIG_ADDRESS, "");
    if (StringUtils.isEmpty(address)) {
      return null;
    }
    String project = properties.getProperty(CommonConfiguration.KEY_SERVICE_PROJECT, "default");
    LOGGER.info("Using config center, address={}", address);
    return new AddressManager(project, Arrays.asList(address.split(",")),EventManager.getEventBus()
            );
  }

  public QueryConfigurationsRequest createQueryConfigurationsRequest() {
    QueryConfigurationsRequest request = new QueryConfigurationsRequest();
    request.setApplication(properties.getProperty(CommonConfiguration.KEY_SERVICE_APPLICATION, "default"));
    request.setServiceName(properties.getProperty(CommonConfiguration.KEY_SERVICE_NAME, "defaultMicroserviceName"));
    request.setVersion(properties.getProperty(CommonConfiguration.KEY_SERVICE_VERSION, "1.0.0.0"));
    request.setEnvironment(properties.getProperty(CommonConfiguration.KEY_SERVICE_ENVIRONMENT, ""));
    // 需要设置为 null， 并且 query 参数为 revision=null 才会返回 revision 信息。 revision = 是不行的。
    request.setRevision(null);
    return request;
  }
}
