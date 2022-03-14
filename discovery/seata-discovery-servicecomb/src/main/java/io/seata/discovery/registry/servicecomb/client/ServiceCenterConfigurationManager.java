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

package io.seata.discovery.registry.servicecomb.client;

import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_INSTANCE_ENVIRONMENT;
import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_INSTANCE_HEALTH_CHECK_INTERVAL;
import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_INSTANCE_HEALTH_CHECK_TIMES;
import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_REGISTRY_ADDRESS;
import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_SERVICE_APPLICATION;
import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_SERVICE_ENVIRONMENT;
import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_SERVICE_NAME;
import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_SERVICE_PROJECT;
import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_SERVICE_VERSION;

public class ServiceCenterConfigurationManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterConfigurationManager.class);

  private Properties environment;

  public ServiceCenterConfigurationManager(Properties environment) {
    this.environment = environment;
  }

  public Microservice createMicroservice() {
    Microservice microservice = new Microservice();
    microservice.setAppId(environment.getProperty(KEY_SERVICE_APPLICATION, "default"));
    microservice.setServiceName(environment.getProperty(KEY_SERVICE_NAME, "defaultMicroserviceName"));
    microservice.setVersion(environment.getProperty(KEY_SERVICE_VERSION, "1.0.0.0"));
    microservice.setEnvironment(environment.getProperty(KEY_SERVICE_ENVIRONMENT, ""));
    Framework framework = new Framework();
    framework.setName("SEATA-DISCOVERY-SERVICECOMB");
    StringBuilder version = new StringBuilder();
    version.append("seata-discovery-servicecomb:");
    version.append(ServiceCenterConfigurationManager.class.getPackage().getImplementationVersion());
    version.append(";");
    framework.setVersion(version.toString());
    microservice.setFramework(framework);
    return microservice;
  }

  public MicroserviceInstance createMicroserviceInstance() {
    MicroserviceInstance instance = new MicroserviceInstance();
    instance.setStatus(MicroserviceInstanceStatus.valueOf(environment.getProperty(KEY_INSTANCE_ENVIRONMENT, "UP")));
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.pull);
    healthCheck.setInterval(Integer.parseInt(environment.getProperty(KEY_INSTANCE_HEALTH_CHECK_INTERVAL, "15")));
    healthCheck.setTimes(Integer.parseInt(environment.getProperty(KEY_INSTANCE_HEALTH_CHECK_TIMES, "3")));
    instance.setHealthCheck(healthCheck);
    return instance;
  }

  public AddressManager createAddressManager() {
    String address = environment.getProperty(KEY_REGISTRY_ADDRESS, "http://127.0.0.1:30100");
    String project = environment.getProperty(KEY_SERVICE_PROJECT, "default");
    LOGGER.info("Using service center, address={}.", address);
    return new AddressManager(project, Arrays.asList(address.split(",")),EventManager.getEventBus());
  }
}
