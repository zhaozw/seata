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
import io.seata.config.Configuration;
import io.seata.config.ConfigurationFactory;
import io.seata.discovery.registry.RegistryService;
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
    private static final Object LOCK_OBJ = new Object();
    private static final Configuration FILE_CONFIG = ConfigurationFactory.CURRENT_FILE_INSTANCE;
    private static volatile ServicecombRegistryServiceImpl instance;

    private static volatile ServicecombRegistryHelper ServicecombRegistryHelper;

    private boolean isServer=false;
    
    private Environment environment = new Environment(FILE_CONFIG);

    private ServicecombRegistryServiceImpl() {
        EventManager.addEventBusClass("com.huaweicloud.common.event.EventManager");
        ServicecombRegistryHelper=new ServicecombRegistryHelper(environment);
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

    /**
     * seata 服务端调用把自己注册到注册中心
     * @param address the address
     * @throws Exception
     */
    @Override
    public void register(InetSocketAddress address) throws Exception {
        NetUtil.validAddress(address);
        isServer=true;
        ServicecombRegistryHelper.setEnableDiscovery(true);
        ServicecombRegistryHelper.register(getEndPoint(address));
    }

    private String getEndPoint(InetSocketAddress address) {
        return "rest://" + address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    /**
     * seata 服务端调用把自己从注册中心注销
     * @param address the address
     * @throws Exception
     */
    @Override
    public void unregister(InetSocketAddress address) throws Exception {
        NetUtil.validAddress(address);
        ServicecombRegistryHelper.unregister(getEndPoint(address));
        EventManager.unregister(this);
    }

    /**
     * 由于事件监听用@Subscribe 实现，所以这个方法不会被调用
     * @param cluster  the cluster
     * @param listener the listener
     * @throws Exception
     */
    @Override
    public void subscribe(String cluster, ServicecombListener listener) throws Exception {
        //LOGGER.info("subscribe");
    }

    /**
     * 由于事件监听用@Subscribe 实现，所以这个方法不会被调用
     * @param cluster  the cluster
     * @param listener the listener
     * @throws Exception
     */
    @Override
    public void unsubscribe(String cluster, ServicecombListener listener) throws Exception {
        //LOGGER.info("unsubscribe");
    }

    /**
     * seata客户端根据key  ${service.vgroup-mapping.default_tx_group} 的值去注册中心查找对应服务器的地址
     * @param key the key
     * @return
     * @throws Exception
     */
    @Override
    public List<InetSocketAddress> lookup(String key) throws Exception {
        String clusterName = getServiceGroup(key);
        if (clusterName == null) {
            return null;
        }
        if (!CURRENT_ADDRESS_MAP.containsKey(clusterName) || CURRENT_ADDRESS_MAP.get(clusterName).isEmpty()) {
            synchronized (LOCK_OBJ) {
                if (!CURRENT_ADDRESS_MAP.containsKey(clusterName) || CURRENT_ADDRESS_MAP.get(clusterName).isEmpty()) {
                    ServicecombRegistryHelper.setEnableDiscovery(false);
                    ServiceCenterClient client =ServicecombRegistryHelper.initClient();
                    List<String> clusters = new ArrayList<>();
                    clusters.add(clusterName);
                    try {
                        List<InetSocketAddress> newAddressList = new ArrayList<>();
                        Map<String, Microservice> result = new HashMap<>();
                        MicroservicesResponse microservicesResponse = client.getMicroserviceList();
                        microservicesResponse.getServices().forEach(service -> {
                            // 先不考虑运行 crossAPP 的场景， 只允许同应用发现
                            if (service.getAppId().equals(environment.getProperty(KEY_SERVICE_APPLICATION, "default"))&&service.getServiceName().equals(clusterName)) {

                                MicroserviceInstancesResponse instancesResponse = client
                                        .getMicroserviceInstanceList(service.getServiceId());
                                if(instancesResponse.getInstances()!=null){
                                    instancesResponse.getInstances().forEach(instance -> {

                                        instance.getEndpoints().forEach(endpoint -> {

                                            URI uri = URI.create(endpoint);
                                            newAddressList.add(new InetSocketAddress(uri.getHost(),uri.getPort()));
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
    public void close() throws Exception {

    }

    // --- 实例发现事件处理 ---- //
    @Subscribe
    public void onInstanceChangedEvent(DiscoveryEvents.InstanceChangedEvent event) {
        //只记录seata server的实例
        if(!CURRENT_ADDRESS_MAP.containsKey(event.getServiceName())){
            return;
        }
        if (event.getInstances()==null){
            CURRENT_ADDRESS_MAP.remove(event.getAppName());
        }else{
            List<InetSocketAddress> newAddressList = new ArrayList<>();
            event.getInstances().forEach(instance -> {

                instance.getEndpoints().forEach(endpoint -> {

                    URI uri = URI.create(endpoint);
                    newAddressList.add(new InetSocketAddress(uri.getHost(),uri.getPort()));
                });
            });
            CURRENT_ADDRESS_MAP.put(event.getAppName(),newAddressList);
        }
    }
}
