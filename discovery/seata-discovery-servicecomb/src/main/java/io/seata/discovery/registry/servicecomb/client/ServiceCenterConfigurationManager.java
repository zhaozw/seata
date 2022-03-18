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

package io.seata.discovery.registry.servicecomb.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
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

/**
 * @author zhaozhongwei22@163.com
 */
public class ServiceCenterConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterConfigurationManager.class);

    private Properties properties;

    public ServiceCenterConfigurationManager(Properties properties) {
        this.properties = properties;
    }

    public Microservice createMicroservice(String frameworkName) {
        Microservice microservice = new Microservice();
        microservice.setAppId(properties.getProperty(KEY_SERVICE_APPLICATION, CommonConfiguration.DEFAULT));
        microservice.setServiceName(properties.getProperty(KEY_SERVICE_NAME, CommonConfiguration.DEFAULT));
        microservice.setVersion(properties.getProperty(KEY_SERVICE_VERSION, CommonConfiguration.DEFAULT_VERSION));
        microservice.setEnvironment(properties.getProperty(KEY_SERVICE_ENVIRONMENT, CommonConfiguration.EMPTY));
        Framework framework = new Framework();
        framework.setName(frameworkName);
        StringBuilder version = new StringBuilder();
        version.append(frameworkName.toLowerCase(Locale.ROOT)).append(CommonConfiguration.COLON);
        if(StringUtils.isEmpty(ServiceCenterConfigurationManager.class.getPackage().getImplementationVersion())){
            version.append(ServiceCenterConfigurationManager.class.getPackage().getImplementationVersion());
        }else{
            version.append(CommonConfiguration.DEFAULT_VERSION);
        }
        version.append(CommonConfiguration.SEMICOLON);
        framework.setVersion(version.toString());
        microservice.setFramework(framework);
        return microservice;
    }

    public MicroserviceInstance createMicroserviceInstance() {
        MicroserviceInstance instance = new MicroserviceInstance();
        instance.setStatus(MicroserviceInstanceStatus.valueOf(properties.getProperty(KEY_INSTANCE_ENVIRONMENT, CommonConfiguration.UP)));
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.pull);
        healthCheck.setInterval(Integer.parseInt(properties.getProperty(KEY_INSTANCE_HEALTH_CHECK_INTERVAL, CommonConfiguration.DEFAULT_INSTANCE_HEALTH_CHECK_INTERVAL)));
        healthCheck.setTimes(Integer.parseInt(properties.getProperty(KEY_INSTANCE_HEALTH_CHECK_TIMES, CommonConfiguration.DEFAULT_INSTANCE_HEALTH_CHECK_TIMES)));
        instance.setHealthCheck(healthCheck);
        return instance;
    }

    public AddressManager createAddressManager() {
        String address = properties.getProperty(KEY_REGISTRY_ADDRESS, CommonConfiguration.DEFAULT_REGISTRY_URL);
        String project = properties.getProperty(KEY_SERVICE_PROJECT, CommonConfiguration.DEFAULT);
        LOGGER.info("Using service center, address={}.", address);
        return new AddressManager(project, Arrays.asList(address.split(CommonConfiguration.COMMA)));
    }
}
