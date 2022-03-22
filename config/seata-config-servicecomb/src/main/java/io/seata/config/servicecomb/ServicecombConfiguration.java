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
import io.seata.config.servicecomb.client.CommonConfiguration;
import io.seata.config.servicecomb.client.EventManager;
import io.seata.config.servicecomb.client.ServicecombConfigurationHelper;
import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    private static final ConcurrentMap<String,
        ConcurrentMap<ConfigurationChangeListener, ConfigurationChangeListener>> CONFIG_LISTENERS_MAP =
            new ConcurrentHashMap<>(MAP_INITIAL_CAPACITY);
    private static volatile ServicecombConfiguration instance;
    private Properties seataConfig = new Properties();
    private ServicecombConfigurationHelper helper;

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
        helper = new ServicecombConfigurationHelper(createProperties());
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
        updatePropertySourceData(event);
    }

    private void updatePropertySourceData(ConfigurationChangedEvent changedEvent) {
        String dataId = getServicecombDataId();
        String group = null;
        String configInfo = null;
        if (changedEvent.getAdded().containsKey(dataId)) {
            configInfo = changedEvent.getAdded().get(dataId).toString();
        } else if (changedEvent.getUpdated().containsKey(dataId)) {
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

            // Get all the monitored dataids and judge whether it has been modified
            for (Map.Entry<String,
                ConcurrentMap<ConfigurationChangeListener, ConfigurationChangeListener>> entry : CONFIG_LISTENERS_MAP
                    .entrySet()) {
                String listenedDataId = entry.getKey();
                String propertyOld = seataConfig.getProperty(listenedDataId, "");
                String propertyNew = seataConfigNew.getProperty(listenedDataId, "");
                if (!propertyOld.equals(propertyNew)) {
                    ConfigurationChangeEvent event = new ConfigurationChangeEvent().setDataId(listenedDataId)
                        .setNewValue(propertyNew).setNamespace(group);

                    ConcurrentMap<ConfigurationChangeListener, ConfigurationChangeListener> configListeners =
                        entry.getValue();
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
        return String.join(ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR, ConfigurationKeys.FILE_ROOT_CONFIG, CONFIG_TYPE,
            SERVICECOMB_DATA_ID_KEY);
    }

    private static String getServicecombDataId() {
        return FILE_CONFIG.getConfig(getServicecombDataIdKey(), DEFAULT_DATA_ID);
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
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESS))) {
            properties.setProperty(CommonConfiguration.KEY_CONFIG_ADDRESS,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESS));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_CUSTOMLABEL))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_KIE_CUSTOMLABEL,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_CUSTOMLABEL));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_CUSTOMLABELVALUE))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_KIE_CUSTOMLABELVALUE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_CUSTOMLABELVALUE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLECUSTOMCONFIG))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLECUSTOMCONFIG,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLECUSTOMCONFIG));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLESERVICECONFIG))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLESERVICECONFIG,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLESERVICECONFIG));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLEAPPCONFIG))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_KIE_ENABLEAPPCONFIG,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_ENABLEAPPCONFIG));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_FRISTPULLREQUIRED))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_KIE_FRISTPULLREQUIRED,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_KIE_FRISTPULLREQUIRED));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_ENABLELONGPOLLING))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_ENABLELONGPOLLING,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_ENABLELONGPOLLING));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_POLLINGWAITSEC))) {
            properties.setProperty(CommonConfiguration.KEY_SERVICE_POLLINGWAITSEC,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_SERVICE_POLLINGWAITSEC));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_CONFIG_FILESOURCE))) {
            properties.setProperty(CommonConfiguration.KEY_CONFIG_FILESOURCE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_CONFIG_FILESOURCE));
        }
        if (!StringUtils.isEmpty(FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESSTYPE))) {
            properties.setProperty(CommonConfiguration.KEY_CONFIG_ADDRESSTYPE,
                FILE_CONFIG.getConfig(SeataServicecombKeys.KEY_CONFIG_ADDRESSTYPE));
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
        return properties;
    }

}
