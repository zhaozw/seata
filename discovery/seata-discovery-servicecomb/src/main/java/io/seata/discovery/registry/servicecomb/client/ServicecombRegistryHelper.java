package io.seata.discovery.registry.servicecomb.client;

import com.google.common.eventbus.Subscribe;
import io.seata.config.ConfigurationFactory;
import io.seata.discovery.registry.servicecomb.client.auth.AuthHeaderProviders;
import org.apache.servicecomb.http.client.common.HttpConfiguration;
import org.apache.servicecomb.service.center.client.*;
import org.apache.servicecomb.service.center.client.model.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 这个类主要是把Servicecomb 注册中心的细节单独出来
 */
public class ServicecombRegistryHelper {

    private ServiceCenterClient client;

    private ServiceCenterRegistration serviceCenterRegistration;

    private ServiceCenterDiscovery serviceCenterDiscovery;

    private ServiceCenterWatch watch;

    private EnvironmentAdapter environment;

    private ServiceCenterConfigurationManager serviceCenterConfigurationManager;

    private Microservice microservice;

    private MicroserviceInstance microserviceInstance;

    private boolean enableDiscovery=true;

    public ServicecombRegistryHelper(EnvironmentAdapter environment) {
        this.environment=environment;
        serviceCenterConfigurationManager=new ServiceCenterConfigurationManager(environment);
        //commonConfiguration = new CommonConfiguration(environment);;
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

        serviceCenterRegistration = new ServiceCenterRegistration(client, new ServiceCenterConfiguration(),
                EventManager.getEventBus());
        serviceCenterRegistration.setMicroservice(microservice);
        serviceCenterRegistration.setMicroserviceInstance(microserviceInstance);
        serviceCenterRegistration.setHeartBeatInterval(microserviceInstance.getHealthCheck().getInterval());
        serviceCenterRegistration.startRegistration();

        if (environment.getProperty(CommonConfiguration
                .KEY_SERVICE_PROJECT,"false").equals("true")){
            watch = new ServiceCenterWatch(serviceCenterConfigurationManager.createAddressManager(),
                    AuthHeaderProviders.createSSLProperties(environment),
                    AuthHeaderProviders.getRequestAuthHeaderProvider(environment),
                    "default", new HashMap<>(), EventManager.getEventBus());
        }
        EventManager.register(this);
    }

    public void unregister(String endPoint) {
        if(serviceCenterRegistration!=null){
            serviceCenterRegistration.stop();
        }
    }

    public ServiceCenterClient initClient() throws Exception {
        if (client == null) {
            synchronized (ServicecombRegistryHelper.class) {
                if (client == null) {

                    try {
                        getClientFromSpringCloud();
                        if (client == null) {
                            AddressManager addressManager = serviceCenterConfigurationManager.createAddressManager();
                            HttpConfiguration.SSLProperties sslProperties = AuthHeaderProviders.createSSLProperties(environment);
                            client = new ServiceCenterClient(addressManager, sslProperties,
                                    AuthHeaderProviders.getRequestAuthHeaderProvider(environment),
                                    "default", null);
                        }
                        microservice = serviceCenterConfigurationManager.createMicroservice();

                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
        return client;
    }

    private void getClientFromSpringCloud() {
        //TODO ? 可不可以通过反射或者单例获取ServiceCenterClient，否则在客户端会被实例化两次，而且配置文件里面也需要配置两次一个是在spring.cloud.servicecomb下面，一个是在seata下面
        //如果再对接一个其它的需要注册中心的框架，是否会重复3次？感觉这个类里面的功能都应该封装到SDK里面会好一些
    }

    @Subscribe
    public void onMicroserviceInstanceRegistrationEvent(RegistrationEvents.MicroserviceInstanceRegistrationEvent event) {
        if(event.isSuccess() && enableDiscovery){
            if (serviceCenterDiscovery == null) {
                serviceCenterDiscovery = new ServiceCenterDiscovery(client, EventManager.getEventBus());
                serviceCenterDiscovery.updateMyselfServiceId(microservice.getServiceId());
                serviceCenterDiscovery
                        .setPollInterval(Integer.parseInt(environment.getProperty(CommonConfiguration.KEY_INSTANCE_PULL_INTERVAL, "15")));
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
