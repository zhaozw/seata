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

package io.seata.config.servicecomb;

import com.google.common.eventbus.Subscribe;
import io.seata.common.exception.NotSupportYetException;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import io.seata.config.AbstractConfiguration;
import io.seata.config.Configuration;
import io.seata.config.ConfigurationChangeEvent;
import io.seata.config.ConfigurationChangeListener;
import io.seata.config.ConfigurationFactory;
import io.seata.config.servicecomb.client.ConfigCenterConfiguration;
import io.seata.config.servicecomb.client.EventManager;
import io.seata.config.servicecomb.client.KieConfigConfiguration;
import io.seata.config.servicecomb.client.auth.AuthHeaderProviders;
import org.apache.http.client.config.RequestConfig;
import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The type Service configuration.
 *
 * @author zhaozhongwei22@163.com
 */
public class ServicecombConfiguration extends AbstractConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombConfiguration.class);

    private static final String CONFIG_TYPE = "servicecomb";
    private static final int MAP_INITIAL_CAPACITY = 8;
    private static final ConcurrentMap<String,
        ConcurrentMap<ConfigurationChangeListener, ConfigurationChangeListener>> CONFIG_LISTENERS_MAP =
            new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);
    private static volatile ServicecombConfiguration instance;
    private Map<String, Object> seataConfig = new HashMap<>();

    volatile ConfigCenterManager configCenterManager;

    volatile KieConfigManager kieConfigManager;

    HttpTransport httpTransport;

    private boolean isKie = false;

    private ConfigCenterConfiguration configCenterConfiguration;

    private KieConfigConfiguration kieConfigConfiguration;

    private KieConfiguration kieConfiguration;

    private ConfigConverter configConverter;

    private Configuration properties = ConfigurationFactory.CURRENT_FILE_INSTANCE;

    /**
     * Get instance of ServicecombConfiguration
     *
     * @return instance
     */
    public static ServicecombConfiguration getInstance() {
        if (instance == null) {
            synchronized (ServicecombConfiguration.class) {
                if (instance == null) {
                    instance = new ServicecombConfiguration();
                }
            }
        }
        return instance;
    }

    /**
     * Instantiates a new Servicecomb configuration.
     */
    private ServicecombConfiguration() {
        configConverter = initConfigConverter();

        configCenterConfiguration = new ConfigCenterConfiguration(properties);
        kieConfigConfiguration = new KieConfigConfiguration(properties);

        initClient(properties);
        initSeataConfig();
        EventManager.register(this);
    }

    @Override
    public String getLatestConfig(String dataId, String defaultValue, long timeoutMills) {
        Object value = seataConfig.get(dataId);
        return value == null ? defaultValue : value.toString();
    }

    @Override
    public boolean putConfig(String dataId, String content, long timeoutMills) {
        throw new NotSupportYetException("not support atomic operation putConfigIfAbsent");
    }

    @Override
    public boolean putConfigIfAbsent(String dataId, String content, long timeoutMills) {
        throw new NotSupportYetException("not support atomic operation putConfigIfAbsent");
    }

    @Override
    public boolean removeConfig(String dataId, long timeoutMills) {
        throw new NotSupportYetException("not support atomic operation putConfigIfAbsent");
    }

    @Override
    public void addConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (StringUtils.isBlank(dataId) || listener == null) {
            return;
        }
        try {
            CONFIG_LISTENERS_MAP.computeIfAbsent(dataId, key -> new ConcurrentHashMap<>(8)).put(listener, listener);
        } catch (Exception exx) {
            LOGGER.error("add servicecomb listener error:{}", exx.getMessage(), exx);
        }
    }

    @Override
    public void removeConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (StringUtils.isBlank(dataId) || listener == null) {
            return;
        }
        Set<ConfigurationChangeListener> configChangeListeners = getConfigListeners(dataId);
        if (CollectionUtils.isNotEmpty(configChangeListeners)) {
            CONFIG_LISTENERS_MAP.get(dataId).remove(listener);
        }
    }

    @Override
    public Set<ConfigurationChangeListener> getConfigListeners(String dataId) {
        Map<ConfigurationChangeListener, ConfigurationChangeListener> configListeners =
            CONFIG_LISTENERS_MAP.get(dataId);
        if (CollectionUtils.isNotEmpty(configListeners)) {
            return configListeners.keySet();
        } else {
            return null;
        }
    }

    @Override
    public String getTypeName() {
        return CONFIG_TYPE;
    }

    @Subscribe
    public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
        if (event.getDeleted() != null) {
            for (String key : event.getDeleted().keySet()) {
                seataConfig.remove(key);
            }
        }
        updateSeataConfig(event.getAdded(), false);
        updateSeataConfig(event.getUpdated(), false);
        updateSeataConfig(event.getDeleted(), true);
    }

    private void updateSeataConfig(Map<String, Object> map, boolean remove) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {

            Object propertyNew = null;
            if (!remove) {
                propertyNew = map.get(entry.getKey());
            }
            ConfigurationChangeEvent event = new ConfigurationChangeEvent().setDataId(entry.getKey())
                .setNewValue(propertyNew != null ? propertyNew.toString() : null);

            ConcurrentMap<ConfigurationChangeListener, ConfigurationChangeListener> configListeners =
                CONFIG_LISTENERS_MAP.get(entry.getKey());
            if (configListeners != null) {
                for (ConfigurationChangeListener configListener : configListeners.keySet()) {
                    configListener.onProcessEvent(event);
                }
            }

        }
    }

    private void initSeataConfig() {
        seataConfig.putAll(configConverter.getCurrentData());
    }

    private void initClient(Configuration properties) {
        isKie = SeataServicecombKeys.KIE
            .equals(properties.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESSTYPE, SeataServicecombKeys.KIE));
        RequestConfig.Builder config = HttpTransportFactory.defaultRequestConfig();

        this.setTimeOut(config);

        httpTransport = HttpTransportFactory.createHttpTransport(AuthHeaderProviders.createSslProperties(properties),
            AuthHeaderProviders.getRequestAuthHeaderProvider(), config.build());

        if (isKie) {
            configKieClient(properties);
        } else {
            configCenterClient(properties);
        }
    }

    private ConfigConverter initConfigConverter() {
        String fileSources =
            properties.getConfig(SeataServicecombKeys.KEY_CONFIG_FILESOURCE, SeataServicecombKeys.EMPTY);
        if (org.apache.commons.lang3.StringUtils.isEmpty(fileSources)) {
            configConverter = new ConfigConverter(null);
        } else {
            configConverter = new ConfigConverter(Arrays.asList(fileSources.split(SeataServicecombKeys.COMMA)));
        }
        return configConverter;
    }

    private void configCenterClient(Configuration properties) {
        QueryConfigurationsRequest queryConfigurationsRequest =
            configCenterConfiguration.createQueryConfigurationsRequest();
        AddressManager addressManager = configCenterConfiguration.createAddressManager();
        if (addressManager == null) {
            LOGGER.warn("Config center address is not configured and will not enable dynamic config.");
            return;
        }
        ConfigCenterClient configCenterClient = new ConfigCenterClient(addressManager, httpTransport);
        try {
            QueryConfigurationsResponse response = configCenterClient.queryConfigurations(queryConfigurationsRequest);
            if (response.isChanged()) {
                configConverter.updateData(response.getConfigurations());
            }
            queryConfigurationsRequest.setRevision(response.getRevision());

        } catch (Exception e) {
            LOGGER.warn("set up Servicesomb configuration failed at startup.", e);
        }
        configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus(), configConverter);
        configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
        configCenterManager.startConfigCenterManager();
    }

    private void configKieClient(Configuration properties) {

        kieConfiguration = kieConfigConfiguration.createKieConfiguration();

        KieAddressManager kieAddressManager = kieConfigConfiguration.createKieAddressManager();
        if (kieAddressManager == null) {
            LOGGER.warn("Kie address is not configured and will not enable dynamic config.");
            return;
        }

        KieClient kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);

        kieConfigManager =
            new KieConfigManager(kieClient, EventManager.getEventBus(), kieConfiguration, configConverter);
        kieConfigManager.firstPull();
        kieConfigManager.startConfigKieManager();
    }

    private void setTimeOut(RequestConfig.Builder config) {
        if (!isKie) {
            return;
        }
        String test =
            properties.getConfig(SeataServicecombKeys.KEY_SERVICE_ENABLELONGPOLLING, SeataServicecombKeys.TRUE);
        if (Boolean.parseBoolean(test)) {
            int pollingWaitInSeconds =
                Integer.valueOf(properties.getConfig(SeataServicecombKeys.KEY_SERVICE_POLLINGWAITSEC,
                    SeataServicecombKeys.DEFAULT_SERVICE_POLLINGWAITSEC));
            config.setSocketTimeout(pollingWaitInSeconds * 1000 + 5000);
        }
    }

}
