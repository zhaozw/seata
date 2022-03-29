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

import io.seata.config.Configuration;
import io.seata.config.servicecomb.SeataServicecombKeys;
import io.seata.config.servicecomb.client.EventManager;
import io.seata.config.servicecomb.client.auth.AuthHeaderProviders;
import io.seata.discovery.registry.servicecomb.client.auth.RBACRequestAuthHeaderProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.apache.servicecomb.service.center.client.model.FindMicroserviceInstancesResponse;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.HealthCheck;
import org.apache.servicecomb.service.center.client.model.HealthCheckMode;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author zhaozhongwei22@163.com
 */
public class ServicecombRegistryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombRegistryHelper.class);

    private ServiceCenterClient client;

    private ServiceCenterRegistration serviceCenterRegistration;

    private ServiceCenterDiscovery serviceCenterDiscovery;

    private ServiceCenterWatch watch;

    private Configuration properties;

    private Microservice microservice;

    private MicroserviceInstance microserviceInstance;

    private final String frameworkName;

    private RequestAuthHeaderProvider requestAuthHeaderProvider;

    public ServicecombRegistryHelper(Configuration properties, String frameworkName) throws Exception {
        this.properties = properties;
        this.frameworkName = frameworkName;
        initClient();
        EventManager.register(this);
    }

    public void register(String endPoint) throws Exception {
        microserviceInstance = createMicroserviceInstance();
        microserviceInstance.setHostName(InetAddress.getLocalHost().getHostName());

        List<String> endPoints = new ArrayList<>();
        endPoints.add(endPoint);
        microserviceInstance.setEndpoints(endPoints);
        String currTime = String.valueOf(System.currentTimeMillis());
        microserviceInstance.setTimestamp(currTime);
        microserviceInstance.setModTimestamp(currTime);

        serviceCenterRegistration =
            new ServiceCenterRegistration(client, new ServiceCenterConfiguration(), EventManager.getEventBus());
        serviceCenterRegistration.setMicroservice(microservice);
        serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
        serviceCenterRegistration.setHeartBeatInterval(microserviceInstance.getHealthCheck().getInterval());
        serviceCenterRegistration.startRegistration();

        if (SeataServicecombKeys.TRUE
            .equals(properties.getConfig(SeataServicecombKeys.KEY_REGISTRY_WATCH, SeataServicecombKeys.FALSE))) {
            watch = new ServiceCenterWatch(createAddressManager(), AuthHeaderProviders.createSslProperties(properties),
                getRequestAuthHeaderProvider(client, properties), SeataServicecombKeys.DEFAULT, Collections.EMPTY_MAP,
                EventManager.getEventBus());
        }
    }

    public void unregister(String endPoint) {

        if (serviceCenterRegistration != null) {
            serviceCenterRegistration.stop();
            if (!StringUtils.isEmpty(microserviceInstance.getInstanceId())) {
                try {
                    client.deleteMicroserviceInstance(microservice.getServiceId(),
                        microserviceInstance.getInstanceId());
                } catch (Exception e) {
                    LOGGER.error("delete microservice failed. ", e);
                }
            }
        }
        if (watch != null) {
            watch.stop();
        }
    }

    private ServiceCenterClient initClient() throws Exception {
        if (client == null) {
            synchronized (ServicecombRegistryHelper.class) {
                if (client == null) {

                    try {
                        AddressManager addressManager = createAddressManager();
                        HttpConfiguration.SSLProperties sslProperties =
                            AuthHeaderProviders.createSslProperties(properties);
                        client = new ServiceCenterClient(addressManager, sslProperties,
                            getRequestAuthHeaderProvider(client, properties), SeataServicecombKeys.DEFAULT, null);
                        microservice = createMicroservice(frameworkName);

                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return client;
    }

    public void startDiscovery(String appId, String serviceId, String serviceName) {
        if (serviceCenterDiscovery == null) {
            serviceCenterDiscovery = new ServiceCenterDiscovery(client, EventManager.getEventBus());
            serviceCenterDiscovery
                .setPollInterval(Integer.parseInt(properties.getConfig(SeataServicecombKeys.KEY_INSTANCE_PULL_INTERVAL,
                    SeataServicecombKeys.DEFAULT_INSTANCE_PULL_INTERVAL)));
            serviceCenterDiscovery.startDiscovery();
        }
        serviceCenterDiscovery.updateMyselfServiceId(serviceId);
        serviceCenterDiscovery.registerIfNotPresent(new ServiceCenterDiscovery.SubscriptionKey(appId, serviceName));
    }

    public RequestAuthHeaderProvider getRequestAuthHeaderProvider(ServiceCenterClient client,
        Configuration properties) {
        if (requestAuthHeaderProvider == null) {
            List<AuthHeaderProvider> authHeaderProviders = new ArrayList<>();
            authHeaderProviders.add(new RBACRequestAuthHeaderProvider(client, properties));
            requestAuthHeaderProvider = AuthHeaderProviders.getRequestAuthHeaderProvider(authHeaderProviders);
        }
        return requestAuthHeaderProvider;
    }

    public Microservice createMicroservice(String frameworkName) {
        Microservice microservice = new Microservice();

        microservice
            .setAppId(properties.getConfig(SeataServicecombKeys.KEY_SERVICE_APPLICATION, SeataServicecombKeys.DEFAULT));
        microservice
            .setServiceName(properties.getConfig(SeataServicecombKeys.KEY_SERVICE_NAME, SeataServicecombKeys.DEFAULT));
        microservice.setVersion(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_VERSION, SeataServicecombKeys.DEFAULT_VERSION));
        microservice.setEnvironment(
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_ENVIRONMENT, SeataServicecombKeys.EMPTY));
        Framework framework = new Framework();
        framework.setName(frameworkName);
        StringBuilder version = new StringBuilder();
        version.append(frameworkName.toLowerCase(Locale.ROOT)).append(SeataServicecombKeys.COLON);
        if (StringUtils.isEmpty(ServicecombRegistryHelper.class.getPackage().getImplementationVersion())) {
            version.append(ServicecombRegistryHelper.class.getPackage().getImplementationVersion());
        } else {
            version.append(SeataServicecombKeys.DEFAULT_VERSION);
        }
        version.append(SeataServicecombKeys.SEMICOLON);
        framework.setVersion(version.toString());
        microservice.setFramework(framework);
        return microservice;
    }

    public MicroserviceInstance createMicroserviceInstance() {
        MicroserviceInstance instance = new MicroserviceInstance();
        instance.setStatus(MicroserviceInstanceStatus
            .valueOf(properties.getConfig(SeataServicecombKeys.KEY_INSTANCE_ENVIRONMENT, SeataServicecombKeys.UP)));
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.pull);
        healthCheck
            .setInterval(Integer.parseInt(properties.getConfig(SeataServicecombKeys.KEY_INSTANCE_HEALTH_CHECK_INTERVAL,
                SeataServicecombKeys.DEFAULT_INSTANCE_HEALTH_CHECK_INTERVAL)));
        healthCheck.setTimes(Integer.parseInt(properties.getConfig(SeataServicecombKeys.KEY_INSTANCE_HEALTH_CHECK_TIMES,
            SeataServicecombKeys.DEFAULT_INSTANCE_HEALTH_CHECK_TIMES)));
        instance.setHealthCheck(healthCheck);
        return instance;
    }

    public AddressManager createAddressManager() {
        String address =
            properties.getConfig(SeataServicecombKeys.KEY_REGISTRY_ADDRESS, SeataServicecombKeys.DEFAULT_REGISTRY_URL);
        String project = properties.getConfig(SeataServicecombKeys.KEY_SERVICE_PROJECT, SeataServicecombKeys.DEFAULT);
        LOGGER.info("Using service center, address={}.", address);
        return new AddressManager(project, Arrays.asList(address.split(SeataServicecombKeys.COMMA)));
    }

    public List<MicroserviceInstance> pullInstance(String appId, String serviceName) {
        if (!StringUtils.isEmpty(microservice.getServiceId())) {
            try {
                FindMicroserviceInstancesResponse instancesResponse = client.findMicroserviceInstance(
                    microservice.getServiceId(), appId, serviceName, SeataServicecombKeys.ALL_VERSION, null);
                if (instancesResponse.isModified()) {
                    List<MicroserviceInstance> instances =
                        instancesResponse.getMicroserviceInstancesResponse().getInstances() == null
                            ? Collections.emptyList()
                            : instancesResponse.getMicroserviceInstancesResponse().getInstances();
                    return instances;
                }
            } catch (Exception e) {
                LOGGER.error("find service {}#{} instance failed.", appId, serviceName, e);
            }
        }
        // registration not ready
        return Collections.emptyList();
    }

    public ServiceCenterClient getClient() {
        return client;
    }

    public Microservice getMicroservice() {
        return microservice;
    }
}
