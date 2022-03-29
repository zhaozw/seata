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

package io.seata.discovery.registry.servicecomb;

import com.google.common.eventbus.Subscribe;
import io.seata.common.ConfigurationKeys;
import io.seata.common.util.NetUtil;
import io.seata.config.Configuration;
import io.seata.config.ConfigurationFactory;
import io.seata.config.servicecomb.SeataServicecombKeys;
import io.seata.config.servicecomb.client.EventManager;
import io.seata.discovery.registry.RegistryService;
import io.seata.discovery.registry.servicecomb.client.ServicecombRegistryHelper;
import org.apache.servicecomb.service.center.client.DiscoveryEvents;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.RegisteredMicroserviceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Servicecomb registry service.
 *
 * @author slievrly
 */
public class ServicecombRegistryServiceImpl implements RegistryService<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombRegistryServiceImpl.class);

    private static final String FRAMEWORK_NAME = "SEATA-DISCOVERY-SERVICECOMB";
    private static final Object LOCK_OBJ = new Object();
    private static final Configuration FILE_CONFIG = ConfigurationFactory.CURRENT_FILE_INSTANCE;
    private static volatile ServicecombRegistryServiceImpl instance;

    private ServicecombRegistryHelper servicecombRegistryHelper;

    Map<String, String> appId2ServiceidMap = new ConcurrentHashMap<>();

    private ServicecombRegistryServiceImpl() throws Exception {
        servicecombRegistryHelper = new ServicecombRegistryHelper(FILE_CONFIG, FRAMEWORK_NAME);
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    static ServicecombRegistryServiceImpl getInstance() {
        if (instance == null) {
            synchronized (ServicecombRegistryServiceImpl.class) {
                if (instance == null) {
                    try {
                        instance = new ServicecombRegistryServiceImpl();
                    } catch (Exception e) {
                        LOGGER.error("Initializing servicecomb registry failed!", e);
                    }
                }
            }
        }
        return instance;
    }

    @Override
    public void register(InetSocketAddress address) throws Exception {
        NetUtil.validAddress(address);
        servicecombRegistryHelper.register(getEndPoint(address));
    }

    private String getEndPoint(InetSocketAddress address) {
        return SeataServicecombKeys.REST_PROTOCOL + address.getAddress().getHostAddress() + SeataServicecombKeys.COLON
            + address.getPort();
    }

    @Override
    public void unregister(InetSocketAddress address) throws Exception {
        NetUtil.validAddress(address);
        servicecombRegistryHelper.unregister(getEndPoint(address));
        EventManager.unregister(this);
    }

    @Override
    public void subscribe(String cluster, Object listener) throws Exception {}

    @Override
    public void unsubscribe(String cluster, Object listener) throws Exception {}

    @Override
    public List<InetSocketAddress> lookup(String key) throws Exception {
        String clusterName = getServiceGroup(key);
        if (clusterName == null) {
            return null;
        }
        if (!CURRENT_ADDRESS_MAP.containsKey(clusterName)) {
            synchronized (LOCK_OBJ) {
                if (!CURRENT_ADDRESS_MAP.containsKey(clusterName)) {
                    EventManager.register(this);
                    ServiceCenterClient client = servicecombRegistryHelper.getClient();
                    try {
                        List<InetSocketAddress> newAddressList = new ArrayList<>();
                        String serverAppId =
                            getServiceGroup(clusterName + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR + "appName");;
                        setServiceId(serverAppId, clusterName, client);
                        if (appId2ServiceidMap.containsKey(clusterName)) {
                            servicecombRegistryHelper.startDiscovery(serverAppId, appId2ServiceidMap.get(clusterName),
                                clusterName);
                            List<MicroserviceInstance> instances =
                                servicecombRegistryHelper.pullInstance(serverAppId, clusterName);

                            instances.forEach(instance -> {
                                instance.getEndpoints().forEach(endpoint -> {
                                    URI uri = URI.create(endpoint);
                                    newAddressList.add(new InetSocketAddress(uri.getHost(), uri.getPort()));
                                });
                            });
                            CURRENT_ADDRESS_MAP.put(clusterName, newAddressList);
                        }
                    } catch (Exception e) {
                        LOGGER.error("lookup cluster name from servicecomb failed.", e);
                    }
                }
            }
        }
        return CURRENT_ADDRESS_MAP.computeIfAbsent(clusterName, k -> new ArrayList<>());
    }

    private void setServiceId(String serverAppId, String clusterName, ServiceCenterClient client) {
        if (appId2ServiceidMap.containsKey(clusterName)) {
            return;
        }
        Microservice microservice = createServerMicroservice(serverAppId, clusterName);
        RegisteredMicroserviceResponse response = client.queryServiceId(microservice);
        if (response != null) {
            microservice.setServiceId(response.getServiceId());
            appId2ServiceidMap.put(clusterName, response.getServiceId());
        }
    }

    @Override
    public void close() throws Exception {}

    /**
     * listen InstanceChangedEvent event,servicecomb uses @Subscribe to deal with event instead of listener class
     * 
     * @param event
     */
    @Subscribe
    public void onInstanceChangedEvent(DiscoveryEvents.InstanceChangedEvent event) {
        if (!CURRENT_ADDRESS_MAP.containsKey(event.getServiceName())) {
            return;
        }
        if (event.getInstances() == null) {
            CURRENT_ADDRESS_MAP.remove(event.getServiceName());
        } else {
            List<InetSocketAddress> newAddressList = new ArrayList<>();
            event.getInstances().forEach(instance -> {

                instance.getEndpoints().forEach(endpoint -> {

                    URI uri = URI.create(endpoint);
                    newAddressList.add(new InetSocketAddress(uri.getHost(), uri.getPort()));
                });
            });
            CURRENT_ADDRESS_MAP.put(event.getServiceName(), newAddressList);
        }
    }

    public Microservice createServerMicroservice(String appId, String serviceName) {
        Microservice microservice = new Microservice();
        microservice.setAppId(appId);
        microservice.setServiceName(serviceName);
        String serverVersion = getServiceGroup(serviceName + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR + "version");
        String serverEnv = getServiceGroup(serviceName + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR + "env");
        microservice.setVersion(serverVersion);
        microservice.setEnvironment(serverEnv);
        return microservice;
    }
}
