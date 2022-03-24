/*

  * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package io.seata.config.servicecomb.client;

/**
 * @author zhaozhongwei22@163.com
 */
public interface CommonConfiguration {
    /**
     * service configuration
     */
    String KEY_SERVICE_PROJECT = "servicecomb.service.project";

    String KEY_SERVICE_APPLICATION = "servicecomb.service.application";

    String KEY_SERVICE_NAME = "servicecomb.service.name";

    String KEY_SERVICE_VERSION = "servicecomb.service.version";

    String KEY_SERVICE_ENVIRONMENT = "servicecomb.service.environment";

    /**
     * registry instance configuration
     */
    String KEY_INSTANCE_ENVIRONMENT = "servicecomb.instance.initialStatus";

    String KEY_INSTANCE_PULL_INTERVAL = "servicecomb.instance.pull.interval";

    String KEY_INSTANCE_HEALTH_CHECK_INTERVAL = "servicecomb.instance.healthCheck.interval";

    String KEY_INSTANCE_HEALTH_CHECK_TIMES = "servicecomb.instance.healthCheck.times";

    /**
     * registry configuration
     */
    String KEY_REGISTRY_ADDRESS = "servicecomb.registry.address";

    String KEY_REGISTRY_WATCH = "servicecomb.registry.watch";

    /**
     * config center configuration
     */
    String KEY_CONFIG_ADDRESSTYPE = "servicecomb.config.type";

    String KEY_CONFIG_FILESOURCE = "servicecomb.config.fileSource";

    String KEY_CONFIG_ADDRESS = "servicecomb.config.address";

    /**
     * kie config center polling configuration
     */
    String KEY_SERVICE_ENABLELONGPOLLING = "servicecomb.config.enableLongPolling";

    String KEY_SERVICE_POLLINGWAITSEC = "servicecomb.config.pollingWaitInSeconds";

    /**
     * kie configuration
     */
    String KEY_SERVICE_KIE_CUSTOMLABEL = "servicecomb.config.customLabel";

    String KEY_SERVICE_KIE_CUSTOMLABELVALUE = "servicecomb.config.customLabelValue";

    String KEY_SERVICE_KIE_FRISTPULLREQUIRED = "servicecomb.config.firstPullRequired";

    String KEY_SERVICE_KIE_ENABLEAPPCONFIG = "servicecomb.config.enableAppConfig";

    String KEY_SERVICE_KIE_ENABLECUSTOMCONFIG = "servicecomb.config.enableCustomConfig";

    String KEY_SERVICE_KIE_ENABLESERVICECONFIG = "servicecomb.config.enableServiceConfig";

    /**
     * ssl configuration
     */
    String KEY_SSL_ENABLED = "servicecomb.ssl.enabled";

    String KEY_SSL_ENGINE = "servicecomb.ssl.engine";

    String KEY_SSL_PROTOCOLS = "servicecomb.ssl.protocols";

    String KEY_SSL_CIPHERS = "servicecomb.ssl.ciphers";

    String KEY_SSL_AUTH_PEER = "servicecomb.ssl.authPeer";

    String KEY_SSL_CHECKCN_HOST = "servicecomb.ssl.checkCNHost";

    String KEY_SSL_CHECKCN_WHITE = "servicecomb.ssl.checkCNWhite";

    String KEY_SSL_CHECKCN_WHITE_FILE = "servicecomb.ssl.checkCNWhiteFile";

    String KEY_SSL_ALLOW_RENEGOTIATE = "servicecomb.ssl.allowRenegotiate";

    String KEY_SSL_STORE_PATH = "servicecomb.ssl.storePath";

    String KEY_SSL_TRUST_STORE = "servicecomb.ssl.trustStore";

    String KEY_SSL_TRUST_STORE_TYPE = "servicecomb.ssl.trustStoreType";

    String KEY_SSL_TRUST_STORE_VALUE = "servicecomb.ssl.trustStoreValue";

    String KEY_SSL_KEYSTORE = "servicecomb.ssl.keyStore";

    String KEY_SSL_KEYSTORE_TYPE = "servicecomb.ssl.keyStoreType";

    String KEY_SSL_KEYSTORE_VALUE = "servicecomb.ssl.keyStoreValue";

    String KEY_SSL_CRL = "servicecomb.ssl.crl";

    String KEY_SSL_SSL_CUSTOM_CLASS = "servicecomb.ssl.sslCustomClass";

    /**
     * RBAC configuration
     */
    String KEY_RBAC_NAME = "servicecomb.credentials.account.name";

    String KEY_RBAC_PASSWORD = "servicecomb.credentials.account.password";

    /**
     * default value
     */
    String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384," + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";
    String TRUE = "true";
    String FALSE = "false";
    String DEFAULT = "default";
    String JDK = "jdk";
    String TLS = "TLSv1.2";
    String PKCS12 = "PKCS12";
    String INTERNAL = "internal";
    String EMPTY = "";
    String UTF_8 = "UTF-8";
    String DEFAULT_VERSION = "1.0.0.0";
    String DEFAULT_CONFIG_URL = "http://127.0.0.1:30110";
    String DEFAULT_REGISTRY_URL = "http://127.0.0.1:30100";
    String COMMA = ",";
    String PUBLIC = "public";
    String DEFAULT_SERVICE_POLLINGWAITSEC="10";
    String KIE = "kie";
    String SEMICOLON = ";";
    String COLON = ":";
    String DEFAULT_INSTANCE_PULL_INTERVAL="15";
    String UP = "UP";
    String DEFAULT_INSTANCE_HEALTH_CHECK_INTERVAL = "15";
    String DEFAULT_INSTANCE_HEALTH_CHECK_TIMES = "3";
    String REST_PROTOCOL = "rest://";
}
