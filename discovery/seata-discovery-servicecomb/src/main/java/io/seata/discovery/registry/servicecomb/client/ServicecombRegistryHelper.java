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
import io.seata.config.servicecomb.client.CommonConfiguration;
import io.seata.config.servicecomb.client.EventManager;
import io.seata.config.servicecomb.client.auth.AuthHeaderProviders;
import io.seata.discovery.registry.servicecomb.client.auth.RBACRequestAuthHeaderProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.RegistrationEvents;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
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

    private Microservice microservice;

    private MicroserviceInstance microserviceInstance;

    private boolean enableDiscovery = true;

    private final String frameworkName;

    private RequestAuthHeaderProvider requestAuthHeaderProvider;

    public ServicecombRegistryHelper(Properties properties,String frameworkName) {
        this.properties = properties;
        this.frameworkName = frameworkName;
    }

    public void register(String endPoint) throws Exception {
        initClient();

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

        if (CommonConfiguration.TRUE.equals(properties.getProperty(CommonConfiguration.KEY_REGISTRY_WATCH, CommonConfiguration.FALSE))) {
            watch = new ServiceCenterWatch(createAddressManager(),
                AuthHeaderProviders.createSslProperties(properties),
                getRequestAuthHeaderProvider(client, properties), CommonConfiguration.DEFAULT, Collections.EMPTY_MAP,
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
                        AddressManager addressManager = createAddressManager();
                        HttpConfiguration.SSLProperties sslProperties =
                        AuthHeaderProviders.createSslProperties(properties);
                        client = new ServiceCenterClient(addressManager, sslProperties,
                            getRequestAuthHeaderProvider(client, properties), CommonConfiguration.DEFAULT, null);
                        microservice = createMicroservice(frameworkName);

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

    public RequestAuthHeaderProvider getRequestAuthHeaderProvider(ServiceCenterClient client, Properties properties) {
        if (requestAuthHeaderProvider==null){
            List<AuthHeaderProvider> authHeaderProviders = new ArrayList<>();
            authHeaderProviders.add(new RBACRequestAuthHeaderProvider(client, properties));
            requestAuthHeaderProvider = AuthHeaderProviders.getRequestAuthHeaderProvider(authHeaderProviders);
        }
        return requestAuthHeaderProvider;
    }
    public Microservice createMicroservice(String frameworkName) {
        Microservice microservice = new Microservice();
        microservice.setAppId(properties.getProperty(CommonConfiguration.KEY_SERVICE_APPLICATION, CommonConfiguration.DEFAULT));
        microservice.setServiceName(properties.getProperty(CommonConfiguration.KEY_SERVICE_NAME, CommonConfiguration.DEFAULT));
        microservice.setVersion(properties.getProperty(CommonConfiguration.KEY_SERVICE_VERSION, CommonConfiguration.DEFAULT_VERSION));
        microservice.setEnvironment(properties.getProperty(CommonConfiguration.KEY_SERVICE_ENVIRONMENT, CommonConfiguration.EMPTY));
        Framework framework = new Framework();
        framework.setName(frameworkName);
        StringBuilder version = new StringBuilder();
        version.append(frameworkName.toLowerCase(Locale.ROOT)).append(CommonConfiguration.COLON);
        if(StringUtils.isEmpty(ServicecombRegistryHelper.class.getPackage().getImplementationVersion())){
            version.append(ServicecombRegistryHelper.class.getPackage().getImplementationVersion());
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
        instance.setStatus(MicroserviceInstanceStatus.valueOf(properties.getProperty(CommonConfiguration.KEY_INSTANCE_ENVIRONMENT, CommonConfiguration.UP)));
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setMode(HealthCheckMode.pull);
        healthCheck.setInterval(Integer.parseInt(properties.getProperty(CommonConfiguration.KEY_INSTANCE_HEALTH_CHECK_INTERVAL, CommonConfiguration.DEFAULT_INSTANCE_HEALTH_CHECK_INTERVAL)));
        healthCheck.setTimes(Integer.parseInt(properties.getProperty(CommonConfiguration.KEY_INSTANCE_HEALTH_CHECK_TIMES, CommonConfiguration.DEFAULT_INSTANCE_HEALTH_CHECK_TIMES)));
        instance.setHealthCheck(healthCheck);
        return instance;
    }

    public AddressManager createAddressManager() {
        String address = properties.getProperty(CommonConfiguration.KEY_REGISTRY_ADDRESS, CommonConfiguration.DEFAULT_REGISTRY_URL);
        String project = properties.getProperty(CommonConfiguration.KEY_SERVICE_PROJECT, CommonConfiguration.DEFAULT);
        LOGGER.info("Using service center, address={}.", address);
        return new AddressManager(project, Arrays.asList(address.split(CommonConfiguration.COMMA)));
    }
}
