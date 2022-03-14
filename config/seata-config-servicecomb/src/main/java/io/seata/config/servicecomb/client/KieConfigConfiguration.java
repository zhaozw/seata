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

import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;


public class KieConfigConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(KieConfigConfiguration.class);

  private Properties environment;

  public KieConfigConfiguration(Properties environment) {
    this.environment = environment;
  }

  //初始化配置属性值，这里统一初始化，对于client模块，解耦对默认值的感知,屏蔽不同框架带来的配置项差异对KieAddressManager的影响
  public KieAddressManager createKieAddressManager() {
    String address = environment.getProperty(CommonConfiguration.KEY_CONFIG_ADDRESS, "http://127.0.0.1:30110");
    if (StringUtils.isEmpty(address)) {
      return null;
    }
    KieAddressManager kieAddressManager = new KieAddressManager(Arrays.asList(address.split(",")),EventManager.getEventBus());
    LOGGER.info("Using kie, address={}", address);
    return kieAddressManager;
  }

  public KieConfiguration createKieConfiguration() {
    KieConfiguration kieConfiguration = new KieConfiguration();
    kieConfiguration.setAppName(environment.getProperty(CommonConfiguration.KEY_SERVICE_APPLICATION, "default"));
    kieConfiguration.setServiceName(environment.getProperty(CommonConfiguration.KEY_SERVICE_NAME, "defaultMicroserviceName"));
    kieConfiguration.setEnvironment(environment.getProperty(CommonConfiguration.KEY_SERVICE_ENVIRONMENT, ""));
    kieConfiguration.setProject(environment.getProperty(CommonConfiguration.KEY_SERVICE_PROJECT, "default"));
    kieConfiguration.setCustomLabel(environment.getProperty(CommonConfiguration.KEY_SERVICE_KIE_CUSTOMLABEL, "public"));
    kieConfiguration.setCustomLabelValue(environment.getProperty(CommonConfiguration.KEY_SERVICE_KIE_CUSTOMLABELVALUE, ""));
    kieConfiguration
        .setEnableCustomConfig(
            Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLECUSTOMCONFIG, "true")));
    kieConfiguration
        .setEnableServiceConfig(
            Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLESERVICECONFIG, "true")));
    kieConfiguration
        .setEnableAppConfig(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLEAPPCONFIG, "true")));
    kieConfiguration
        .setFirstPullRequired(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SERVICE_KIE_FRISTPULLREQUIRED, "true")));
    kieConfiguration
        .setEnableLongPolling(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING, "true")));
    kieConfiguration
        .setPollingWaitInSeconds(Integer.parseInt(environment.getProperty(CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC, "10")));
    return kieConfiguration;
  }
}
