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
import io.seata.common.util.NetUtil;
import io.seata.common.util.StringUtils;
import io.seata.config.Configuration;
import io.seata.config.ConfigurationFactory;
import io.seata.discovery.registry.RegistryService;
import io.seata.discovery.registry.servicecomb.client.CommonConfiguration;
import io.seata.discovery.registry.servicecomb.client.EventManager;
import io.seata.discovery.registry.servicecomb.client.ServicecombRegistryHelper;
import org.apache.servicecomb.service.center.client.*;
import org.apache.servicecomb.service.center.client.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;

import static io.seata.discovery.registry.servicecomb.client.CommonConfiguration.KEY_SERVICE_APPLICATION;

/**
 * The type Servicecomb registry service.
 *
 * @author slievrly
 */
public class ServicecombRegistryServiceImpl implements RegistryService<ServicecombListener> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombRegistryServiceImpl.class);

    private static final String FRAMEWORK_NAME = "SEATA-DISCOVERY-SERVICECOMB";
    private static final Object LOCK_OBJ = new Object();
    private static final Configuration FILE_CONFIG = ConfigurationFactory.CURRENT_FILE_INSTANCE;
    private static volatile ServicecombRegistryServiceImpl instance;

    private static volatile ServicecombRegistryHelper ServicecombRegistryHelper;

    private boolean isServer = false;

    private Properties properties;

    private ServicecombRegistryServiceImpl() {
        properties = createProperties();
        ServicecombRegistryHelper = new ServicecombRegistryHelper(properties, FRAMEWORK_NAME);
        EventManager.register(this);
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
                    instance = new ServicecombRegistryServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void register(InetSocketAddress address) throws Exception {
        NetUtil.validAddress(address);
        isServer = true;
        ServicecombRegistryHelper.setEnableDiscovery(true);
        ServicecombRegistryHelper.register(getEndPoint(address));
    }

    private String getEndPoint(InetSocketAddress address) {
        return CommonConfiguration.REST_PROTOCOL + address.getAddress().getHostAddress() + CommonConfiguration.COLON
            + address.getPort();
    }

    @Override
    public void unregister(InetSocketAddress address) throws Exception {
        NetUtil.validAddress(address);
        ServicecombRegistryHelper.unregister(getEndPoint(address));
        EventManager.unregister(this);
    }

    @Override
    public void subscribe(String cluster, ServicecombListener listener) throws Exception {}

    @Override
    public void unsubscribe(String cluster, ServicecombListener listener) throws Exception {}

    @Override
    public List<InetSocketAddress> lookup(String key) throws Exception {
        String clusterName = getServiceGroup(key);
        if (clusterName == null) {
            return null;
        }
        if (!CURRENT_ADDRESS_MAP.containsKey(clusterName)) {
            synchronized (LOCK_OBJ) {
                if (!CURRENT_ADDRESS_MAP.containsKey(clusterName)) {
                    ServicecombRegistryHelper.setEnableDiscovery(false);
                    ServiceCenterClient client = ServicecombRegistryHelper.initClient();
                    List<String> clusters = new ArrayList<>();
                    clusters.add(clusterName);
                    try {
                        List<InetSocketAddress> newAddressList = new ArrayList<>();
                        MicroservicesResponse microservicesResponse = client.getMicroserviceList();
                        microservicesResponse.getServices().forEach(service -> {
                            if (service.getAppId()
                                .equals(properties.getProperty(KEY_SERVICE_APPLICATION, CommonConfiguration.DEFAULT))
                                && service.getServiceName().equals(clusterName)) {

                                MicroserviceInstancesResponse instancesResponse =
                                    client.getMicroserviceInstanceList(service.getServiceId());
                                if (instancesResponse.getInstances() != null) {
                                    instancesResponse.getInstances().forEach(instance -> {

                                        instance.getEndpoints().forEach(endpoint -> {

                                            URI uri = URI.create(endpoint);
                                            newAddressList.add(new InetSocketAddress(uri.getHost(), uri.getPort()));
                                        });
                                    });
                                }
                            }
                        });
                        CURRENT_ADDRESS_MAP.put(clusterName, newAddressList);
                    } catch (Exception e) {
                        LOGGER.error("update interface - service name map failed.", e);
                    }
                }
            }
        }
        return CURRENT_ADDRESS_MAP.computeIfAbsent(clusterName, k -> new ArrayList<>());
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
            CURRENT_ADDRESS_MAP.remove(event.getAppName());
        } else {
            List<InetSocketAddress> newAddressList = new ArrayList<>();
            event.getInstances().forEach(instance -> {

                instance.getEndpoints().forEach(endpoint -> {

                    URI uri = URI.create(endpoint);
                    newAddressList.add(new InetSocketAddress(uri.getHost(), uri.getPort()));
                });
            });
            CURRENT_ADDRESS_MAP.put(event.getAppName(), newAddressList);
        }
    }

    private Properties createProperties() {
        Properties properties = new Properties();
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_PROJECT))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_PROJECT,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_PROJECT));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_APPLICATION))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_APPLICATION,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_APPLICATION));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_NAME))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_NAME,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_NAME));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_VERSION))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_VERSION,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_VERSION));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_ENVIRONMENT))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_ENVIRONMENT,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_ENVIRONMENT));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_ENABLED))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_ENABLED,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_ENABLED));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_ENGINE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_ENGINE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_ENGINE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_PROTOCOLS))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_PROTOCOLS,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_PROTOCOLS));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CIPHERS))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_CIPHERS,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CIPHERS));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_AUTH_PEER))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_AUTH_PEER,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_AUTH_PEER));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_HOST))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_CHECKCN_HOST,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_HOST));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_WHITE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_CHECKCN_WHITE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_WHITE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_WHITE_FILE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_CHECKCN_WHITE_FILE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CHECKCN_WHITE_FILE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_ALLOW_RENEGOTIATE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_ALLOW_RENEGOTIATE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_ALLOW_RENEGOTIATE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_STORE_PATH))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_STORE_PATH,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_STORE_PATH));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_KEYSTORE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE_TYPE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_KEYSTORE_TYPE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE_TYPE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE_VALUE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_KEYSTORE_VALUE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_KEYSTORE_VALUE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_TRUST_STORE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE_TYPE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_TRUST_STORE_TYPE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE_TYPE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE_VALUE))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_TRUST_STORE_VALUE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_TRUST_STORE_VALUE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CRL))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_CRL,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_CRL));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_SSL_CUSTOM_CLASS))) {
            properties.setProperty(CommonConfiguration.KEY_SSL_SSL_CUSTOM_CLASS,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SSL_SSL_CUSTOM_CLASS));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_INSTANCE_ENVIRONMENT))) {
            properties.setProperty(CommonConfiguration.KEY_INSTANCE_ENVIRONMENT,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_INSTANCE_ENVIRONMENT));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_INSTANCE_PULL_INTERVAL))) {
            properties.setProperty(CommonConfiguration.KEY_INSTANCE_PULL_INTERVAL,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_INSTANCE_PULL_INTERVAL));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_INSTANCE_HEALTH_CHECK_INTERVAL))) {
            properties.setProperty(CommonConfiguration.KEY_INSTANCE_HEALTH_CHECK_INTERVAL,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_INSTANCE_HEALTH_CHECK_INTERVAL));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_INSTANCE_HEALTH_CHECK_TIMES))) {
            properties.setProperty(CommonConfiguration.KEY_INSTANCE_HEALTH_CHECK_TIMES,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_INSTANCE_HEALTH_CHECK_TIMES));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_REGISTRY_ADDRESS))) {
            properties.setProperty(CommonConfiguration.KEY_REGISTRY_ADDRESS,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_REGISTRY_ADDRESS));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_REGISTRY_WATCH))) {
            properties.setProperty(CommonConfiguration.KEY_REGISTRY_WATCH,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_REGISTRY_WATCH));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_RBAC_NAME))) {
            properties.setProperty(CommonConfiguration.KEY_RBAC_NAME,
                    FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_RBAC_NAME));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_RBAC_PASSWORD))) {
            properties.setProperty(CommonConfiguration.KEY_RBAC_PASSWORD,
                    FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_RBAC_PASSWORD));
        }
        return properties;
    }
}
