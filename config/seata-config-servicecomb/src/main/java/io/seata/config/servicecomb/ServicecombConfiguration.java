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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.eventbus.Subscribe;
import io.seata.common.ConfigurationKeys;
import io.seata.common.exception.NotSupportYetException;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import io.seata.config.AbstractConfiguration;
import io.seata.config.Configuration;
import io.seata.config.ConfigurationChangeEvent;
import io.seata.config.ConfigurationChangeListener;
import io.seata.config.ConfigurationFactory;
import io.seata.config.processor.ConfigProcessor;
import io.seata.config.servicecomb.client.*;
import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The type Service configuration.
 *
 * @author zhaozw
 */
public class ServicecombConfiguration extends AbstractConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombConfiguration.class);

    private static final String DEFAULT_DATA_ID = "seata.properties";
    private static final String CONFIG_TYPE = "servicecomb";
    private static final String SERVICECOMB_DATA_ID_KEY = "dataId";
    private static final Configuration FILE_CONFIG = ConfigurationFactory.CURRENT_FILE_INSTANCE;
    private static final int MAP_INITIAL_CAPACITY = 8;
    private static final ConcurrentMap<String, ConcurrentMap<ConfigurationChangeListener, ConfigurationChangeListener>> CONFIG_LISTENERS_MAP
            = new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);
    private static volatile ServicecombConfiguration instance;
    private static volatile Properties seataConfig = new Properties();
    private static volatile ServicecombConfigurationHelper helper;

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
        EventManager.addEventBusClass("com.huaweicloud.common.event.EventManager");
        helper = new ServicecombConfigurationHelper(new Environment(FILE_CONFIG));
        initSeataConfig(helper.getCurrentData());
        EventManager.register(this);
    }

    @Override
    public String getLatestConfig(String dataId, String defaultValue, long timeoutMills) {
        String value = seataConfig.getProperty(dataId);
        return value == null ? defaultValue : value;
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
            CONFIG_LISTENERS_MAP.computeIfAbsent(dataId, key -> new ConcurrentHashMap<>())
                    .put(listener, listener);
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
        Map<ConfigurationChangeListener, ConfigurationChangeListener> configListeners = CONFIG_LISTENERS_MAP.get(dataId);
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
        updatePropertySourceData(event);
    }

    private void updatePropertySourceData(ConfigurationChangedEvent changedEvent) {
        String dataId = getServicecombDataId();
        String group = null;
        String configInfo = null;
        if (changedEvent.getAdded().containsKey(dataId)) {
            configInfo = changedEvent.getAdded().get(dataId).toString();
        }else if (changedEvent.getUpdated().containsKey(dataId)) {
            configInfo = changedEvent.getUpdated().get(dataId).toString();
        }
        if (configInfo != null) {
            Properties seataConfigNew = new Properties();
            if (StringUtils.isNotBlank(configInfo)) {
                try {
                    seataConfigNew = ConfigProcessor.processConfig(configInfo, getServicecombDataType());
                } catch (IOException e) {
                    LOGGER.error("load config properties error", e);
                    return;
                }
            }

            //Get all the monitored dataids and judge whether it has been modified
            for (Map.Entry<String, ConcurrentMap<ConfigurationChangeListener, ConfigurationChangeListener>> entry : CONFIG_LISTENERS_MAP.entrySet()) {
                String listenedDataId = entry.getKey();
                String propertyOld = seataConfig.getProperty(listenedDataId, "");
                String propertyNew = seataConfigNew.getProperty(listenedDataId, "");
                if (!propertyOld.equals(propertyNew)) {
                    ConfigurationChangeEvent event = new ConfigurationChangeEvent()
                            .setDataId(listenedDataId)
                            .setNewValue(propertyNew)
                            .setNamespace(group);

                    ConcurrentMap<ConfigurationChangeListener, ConfigurationChangeListener> configListeners = entry.getValue();
                    for (ConfigurationChangeListener configListener : configListeners.keySet()) {
                        configListener.onProcessEvent(event);
                    }
                }
            }

            seataConfig = seataConfigNew;
        }
    }

    private void initSeataConfig(Map<String, Object> currentData) {
        String dataId = getServicecombDataId();
        if (currentData.containsKey(dataId)) {
            String configInfo = currentData.get(dataId).toString();
            try {
                seataConfig = ConfigProcessor.processConfig(configInfo, getServicecombDataType());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getServicecombDataType() {
        return ConfigProcessor.resolverConfigDataType(getServicecombDataId());
    }

    public static String getServicecombDataIdKey() {
        return String.join(ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR, ConfigurationKeys.FILE_ROOT_CONFIG, CONFIG_TYPE, SERVICECOMB_DATA_ID_KEY);
    }

    private static String getServicecombDataId() {
        return FILE_CONFIG.getConfig(getServicecombDataIdKey(), DEFAULT_DATA_ID);
    }

}
