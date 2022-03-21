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

import com.google.common.eventbus.Subscribe;
import io.seata.discovery.registry.servicecomb.client.auth.AuthHeaderProviders;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.http.client.common.HttpConfiguration;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.RegistrationEvents;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author zhaozhongwei22@163.com
 */
public class ServicecombRegistryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombRegistryHelper.class);

    private ServiceCenterClient client;

    private ServiceCenterRegistration serviceCenterRegistration;

    private ServiceCenterDiscovery serviceCenterDiscovery;

    private ServiceCenterWatch watch;

    private Properties properties;

    private ServiceCenterConfigurationManager serviceCenterConfigurationManager;

    private Microservice microservice;

    private MicroserviceInstance microserviceInstance;

    private boolean enableDiscovery = true;

    private final String frameworkName;

    public ServicecombRegistryHelper(Properties properties,String frameworkName) {
        this.properties = properties;
        this.frameworkName = frameworkName;
        serviceCenterConfigurationManager = new ServiceCenterConfigurationManager(properties);
    }

    public void register(String endPoint) throws Exception {
        initClient();

        microserviceInstance = serviceCenterConfigurationManager.createMicroserviceInstance();
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

        if (CommonConfiguration.TRUE.equals(properties.getProperty(CommonConfiguration.KEY_REGISTRY_WATCH, CommonConfiguration.FALSE))) {
            watch = new ServiceCenterWatch(serviceCenterConfigurationManager.createAddressManager(),
                AuthHeaderProviders.createSslProperties(properties),
                AuthHeaderProviders.getRequestAuthHeaderProvider(client, properties), CommonConfiguration.DEFAULT, Collections.EMPTY_MAP,
                EventManager.getEventBus());
        }
        EventManager.register(this);
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

    public ServiceCenterClient initClient() throws Exception {
        if (client == null) {
            synchronized (ServicecombRegistryHelper.class) {
                if (client == null) {

                    try {
                        AddressManager addressManager = serviceCenterConfigurationManager.createAddressManager();
                        HttpConfiguration.SSLProperties sslProperties =
                            AuthHeaderProviders.createSslProperties(properties);
                        client = new ServiceCenterClient(addressManager, sslProperties,
                            AuthHeaderProviders.getRequestAuthHeaderProvider(client, properties), CommonConfiguration.DEFAULT, null);
                        microservice = serviceCenterConfigurationManager.createMicroservice(frameworkName);

                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return client;
    }

    @Subscribe
    public void
        onMicroserviceInstanceRegistrationEvent(RegistrationEvents.MicroserviceInstanceRegistrationEvent event) {
        if (event.isSuccess() && enableDiscovery) {
            if (serviceCenterDiscovery == null) {
                serviceCenterDiscovery = new ServiceCenterDiscovery(client, EventManager.getEventBus());
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
                serviceCenterDiscovery.setPollInterval(
                    Integer.parseInt(properties.getProperty(CommonConfiguration.KEY_INSTANCE_PULL_INTERVAL, CommonConfiguration.DEFAULT_INSTANCE_PULL_INTERVAL)));
                serviceCenterDiscovery.startDiscovery();
            } else {
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
            }
        }
    }

    public void setEnableDiscovery(boolean enableDiscovery) {
        this.enableDiscovery = enableDiscovery;
    }
}
