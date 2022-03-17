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

import io.seata.common.ConfigurationKeys;

/**
 * @author zhaozhongwei22@163.com
 */
public interface SeataServicecombKeys {
    String REGISTRY_TYPE = "servicecomb";

    String CONFIG_KEY_PREFIX = ConfigurationKeys.FILE_ROOT_CONFIG + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR
        + REGISTRY_TYPE + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR;

    String REGISTRY_KEY_PREFIX = ConfigurationKeys.FILE_ROOT_REGISTRY + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR
        + REGISTRY_TYPE + ConfigurationKeys.FILE_CONFIG_SPLIT_CHAR;

    /**
     * service configuration
     */
    String KEY_SERVICE_PROJECT = REGISTRY_KEY_PREFIX + "project";

    String KEY_SERVICE_APPLICATION = REGISTRY_KEY_PREFIX + "application";

    String KEY_SERVICE_NAME = REGISTRY_KEY_PREFIX + "name";

    String KEY_SERVICE_VERSION = REGISTRY_KEY_PREFIX + "version";

    String KEY_SERVICE_ENVIRONMENT = REGISTRY_KEY_PREFIX + "environment";

    /**
     * config center configuration
     */
    String KEY_CONFIG_ADDRESSTYPE = CONFIG_KEY_PREFIX + "type";

    String KEY_CONFIG_FILESOURCE = CONFIG_KEY_PREFIX + "fileSource";

    String KEY_CONFIG_ADDRESS = CONFIG_KEY_PREFIX + "address";

    /**
     * kie config center polling configuration
     */
    String KEY_SERVICE_ENABLELONGPOLLING = REGISTRY_KEY_PREFIX + "enableLongPolling";

    String KEY_SERVICE_POLLINGWAITSEC = REGISTRY_KEY_PREFIX + "pollingWaitInSeconds";

    /**
     * kie configuration
     */
    String KEY_SERVICE_KIE_CUSTOMLABEL = CONFIG_KEY_PREFIX + "customLabel";

    String KEY_SERVICE_KIE_CUSTOMLABELVALUE = CONFIG_KEY_PREFIX + "customLabelValue";

    String KEY_SERVICE_KIE_FRISTPULLREQUIRED = CONFIG_KEY_PREFIX + "firstPullRequired";

    String KEY_SERVICE_KIE_ENABLEAPPCONFIG = CONFIG_KEY_PREFIX + "enableAppConfig";

    String KEY_SERVICE_KIE_ENABLECUSTOMCONFIG = CONFIG_KEY_PREFIX + "enableCustomConfig";

    String KEY_SERVICE_KIE_ENABLESERVICECONFIG = CONFIG_KEY_PREFIX + "enableServiceConfig";

    /**
     * ssl configuration
     */
    String KEY_SSL_ENABLED = REGISTRY_KEY_PREFIX + "ssl.enabled";

    String KEY_SSL_ENGINE = REGISTRY_KEY_PREFIX + "ssl.engine";

    String KEY_SSL_PROTOCOLS = REGISTRY_KEY_PREFIX + "ssl.protocols";

    String KEY_SSL_CIPHERS = REGISTRY_KEY_PREFIX + "ssl.ciphers";

    String KEY_SSL_AUTH_PEER = REGISTRY_KEY_PREFIX + "ssl.authPeer";

    String KEY_SSL_CHECKCN_HOST = REGISTRY_KEY_PREFIX + "ssl.checkCNHost";

    String KEY_SSL_CHECKCN_WHITE = REGISTRY_KEY_PREFIX + "ssl.checkCNWhite";

    String KEY_SSL_CHECKCN_WHITE_FILE = REGISTRY_KEY_PREFIX + "ssl.checkCNWhiteFile";

    String KEY_SSL_ALLOW_RENEGOTIATE = REGISTRY_KEY_PREFIX + "ssl.allowRenegotiate";

    String KEY_SSL_STORE_PATH = REGISTRY_KEY_PREFIX + "ssl.storePath";

    String KEY_SSL_TRUST_STORE = REGISTRY_KEY_PREFIX + "ssl.trustStore";

    String KEY_SSL_TRUST_STORE_TYPE = REGISTRY_KEY_PREFIX + "ssl.trustStoreType";

    String KEY_SSL_TRUST_STORE_VALUE = REGISTRY_KEY_PREFIX + "ssl.trustStoreValue";

    String KEY_SSL_KEYSTORE = REGISTRY_KEY_PREFIX + "ssl.keyStore";

    String KEY_SSL_KEYSTORE_TYPE = REGISTRY_KEY_PREFIX + "ssl.keyStoreType";

    String KEY_SSL_KEYSTORE_VALUE = REGISTRY_KEY_PREFIX + "ssl.keyStoreValue";

    String KEY_SSL_CRL = REGISTRY_KEY_PREFIX + "ssl.crl";

    String KEY_SSL_SSL_CUSTOM_CLASS = REGISTRY_KEY_PREFIX + "ssl.sslCustomClass";

    /**
     * ak / ak configuration
     */
    String KEY_AK_SK_ENABLED = REGISTRY_KEY_PREFIX + "credentials.enabled";

    String KEY_AK_SK_ACCESS_KEY = REGISTRY_KEY_PREFIX + "credentials.accessKey";

    String KEY_AK_SK_SECRET_KEY = REGISTRY_KEY_PREFIX + "credentials.secretKey";

    String KEY_AK_SK_CIPHER = REGISTRY_KEY_PREFIX + "credentials.cipher";

    String KEY_AK_SK_PROJECT = REGISTRY_KEY_PREFIX + "credentials.project";
}
