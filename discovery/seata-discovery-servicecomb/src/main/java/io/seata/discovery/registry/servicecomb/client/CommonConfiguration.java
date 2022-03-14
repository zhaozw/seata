/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seata.discovery.registry.servicecomb.client;

public interface CommonConfiguration {
  String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,"
          + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

  String DEFAULT_PROJECT = "default";

  // ###### service configuration ############### //
  String KEY_SERVICE_PROJECT = "servicecomb.service.project";

  String KEY_SERVICE_APPLICATION = "servicecomb.service.application";

  String KEY_SERVICE_NAME = "servicecomb.service.name";

  String KEY_SERVICE_VERSION = "servicecomb.service.version";

  String KEY_SERVICE_ENVIRONMENT = "servicecomb.service.environment";

  // ###### service instance configuration ############### //
  String KEY_INSTANCE_ENVIRONMENT = "servicecomb.instance.initialStatus";

  String KEY_INSTANCE_PULL_INTERVAL = "servicecomb.instance.pull.interval";

  String KEY_INSTANCE_HEALTH_CHECK_INTERVAL = "servicecomb.instance.healthCheck.interval";

  String KEY_INSTANCE_HEALTH_CHECK_TIMES = "servicecomb.instance.healthCheck.times";

  // ###### service center configuration ############### //
  String KEY_REGISTRY_ADDRESS = "servicecomb.registry.address";

  String KEY_REGISTRY_WATCH = "servicecomb.registry.watch";

  String KEY_SERVICE_IGNORESWAGGERDIFFERENT = "servicecomb.registry.ignoreSwaggerDifferent";

  // ###### config center configuration ############### //
  String KEY_CONFIG_ADDRESSTYPE = "servicecomb.config.type";

  String KEY_CONFIG_FILESOURCE = "servicecomb.config.fileSource";

  String KEY_CONFIG_ADDRESS = "servicecomb.config.address";

  // ###### kie config center polling configuration############### //
  String KEY_SERVICE_ENABLELONGPOLLING = "servicecomb.config.enableLongPolling";

  String KEY_SERVICE_POLLINGWAITSEC = "servicecomb.config.pollingWaitInSeconds";

  // ###### kie configuration############### //

  String KEY_SERVICE_KIE_CUSTOMLABEL = "servicecomb.config.customLabel";

  String KEY_SERVICE_KIE_CUSTOMLABELVALUE = "servicecomb.config.customLabelValue";

  String KEY_SERVICE_KIE_FRISTPULLREQUIRED = "servicecomb.config.firstPullRequired";

  String KEY_SERVICE_KIE_ENABLEAPPCONFIG = "servicecomb.config.enableAppConfig";

  String KEY_SERVICE_KIE_ENABLECUSTOMCONFIG = "servicecomb.config.enableCustomConfig";

  String KEY_SERVICE_KIE_ENABLESERVICECONFIG = "servicecomb.config.enableServiceConfig";

  // ###### ssl configuration ############### //
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

  // ###### ak / ak configuration ############### //
  String KEY_AK_SK_ENABLED = "servicecomb.credentials.enabled";

  String KEY_AK_SK_ACCESS_KEY = "servicecomb.credentials.accessKey";

  String KEY_AK_SK_SECRET_KEY = "servicecomb.credentials.secretKey";

  String KEY_AK_SK_CIPHER = "servicecomb.credentials.cipher";

  String KEY_AK_SK_PROJECT = "servicecomb.credentials.project";

  // ###### RBAC configuration ############### //

  String KEY_RBAC_NAME = "servicecomb.credentials.account.name";

  String KEY_RBAC_PASSWORD = "servicecomb.credentials.account.password";


}
