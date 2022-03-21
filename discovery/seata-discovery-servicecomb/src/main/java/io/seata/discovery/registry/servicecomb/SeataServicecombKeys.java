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

import io.seata.common.ConfigurationKeys;

/**
 * servicecomb properties configuration path
 *
 * @author zhaozhongwei22@163.com
 */
public interface SeataServicecombKeys {
    String REGISTRY_TYPE = "servicecomb";

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
     * registry configuration
     */
    String KEY_INSTANCE_ENVIRONMENT = REGISTRY_KEY_PREFIX + "initialStatus";

    String KEY_INSTANCE_PULL_INTERVAL = REGISTRY_KEY_PREFIX + "pull.interval";

    String KEY_INSTANCE_HEALTH_CHECK_INTERVAL = REGISTRY_KEY_PREFIX + "healthcheck.interval";

    String KEY_INSTANCE_HEALTH_CHECK_TIMES = REGISTRY_KEY_PREFIX + "healthcheck.times";

    String KEY_REGISTRY_ADDRESS = REGISTRY_KEY_PREFIX + "address";

    String KEY_REGISTRY_WATCH = REGISTRY_KEY_PREFIX + "watch";

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
     * RBAC configuration
     */
    String KEY_RBAC_NAME = REGISTRY_KEY_PREFIX + "credentials.account.name";

    String KEY_RBAC_PASSWORD = REGISTRY_KEY_PREFIX + "credentials.account.password";
}
