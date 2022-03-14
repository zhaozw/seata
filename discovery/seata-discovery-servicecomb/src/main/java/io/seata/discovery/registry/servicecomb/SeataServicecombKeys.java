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

package io.seata.discovery.registry.servicecomb;


public interface SeataServicecombKeys {
  //String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,"
  //        + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

  //String DEFAULT_PROJECT = "default";
  String FILE_ROOT_REGISTRY = "registry";
  String FILE_CONFIG_SPLIT_CHAR = ".";
  String REGISTRY_TYPE = "servicecomb";

  String FILE_CONFIG_KEY_PREFIX = FILE_ROOT_REGISTRY + FILE_CONFIG_SPLIT_CHAR + REGISTRY_TYPE
          + FILE_CONFIG_SPLIT_CHAR;

  // ###### service configuration ############### //
  String KEY_SERVICE_PROJECT = FILE_CONFIG_KEY_PREFIX + "project";

  String KEY_SERVICE_APPLICATION = FILE_CONFIG_KEY_PREFIX + "application";

  String KEY_SERVICE_NAME = FILE_CONFIG_KEY_PREFIX + "name";

  String KEY_SERVICE_VERSION = FILE_CONFIG_KEY_PREFIX + "version";

  String KEY_SERVICE_ENVIRONMENT = FILE_CONFIG_KEY_PREFIX + "environment";

  // ###### service instance configuration ############### //
  String KEY_INSTANCE_ENVIRONMENT = FILE_CONFIG_KEY_PREFIX + "initialStatus";

  String KEY_INSTANCE_PULL_INTERVAL = FILE_CONFIG_KEY_PREFIX + "pull.interval";

  String KEY_INSTANCE_HEALTH_CHECK_INTERVAL = FILE_CONFIG_KEY_PREFIX + "healthcheck.interval";

  String KEY_INSTANCE_HEALTH_CHECK_TIMES = FILE_CONFIG_KEY_PREFIX + "healthcheck.times";

  // ###### service center configuration ############### //
  String KEY_REGISTRY_ADDRESS = FILE_CONFIG_KEY_PREFIX + "address";

  String KEY_REGISTRY_WATCH = FILE_CONFIG_KEY_PREFIX + "watch";
  // ###### ssl configuration ############### //
  String KEY_SSL_ENABLED = FILE_CONFIG_KEY_PREFIX + "ssl.enabled";

  String KEY_SSL_ENGINE = FILE_CONFIG_KEY_PREFIX + "ssl.engine";

  String KEY_SSL_PROTOCOLS = FILE_CONFIG_KEY_PREFIX + "ssl.protocols";

  String KEY_SSL_CIPHERS = FILE_CONFIG_KEY_PREFIX + "ssl.ciphers";

  String KEY_SSL_AUTH_PEER = FILE_CONFIG_KEY_PREFIX + "ssl.authPeer";

  String KEY_SSL_CHECKCN_HOST = FILE_CONFIG_KEY_PREFIX + "ssl.checkCNHost";

  String KEY_SSL_CHECKCN_WHITE = FILE_CONFIG_KEY_PREFIX + "ssl.checkCNWhite";

  String KEY_SSL_CHECKCN_WHITE_FILE = FILE_CONFIG_KEY_PREFIX + "ssl.checkCNWhiteFile";

  String KEY_SSL_ALLOW_RENEGOTIATE = FILE_CONFIG_KEY_PREFIX + "ssl.allowRenegotiate";

  String KEY_SSL_STORE_PATH = FILE_CONFIG_KEY_PREFIX + "ssl.storePath";

  String KEY_SSL_TRUST_STORE = FILE_CONFIG_KEY_PREFIX + "ssl.trustStore";

  String KEY_SSL_TRUST_STORE_TYPE = FILE_CONFIG_KEY_PREFIX + "ssl.trustStoreType";

  String KEY_SSL_TRUST_STORE_VALUE = FILE_CONFIG_KEY_PREFIX + "ssl.trustStoreValue";

  String KEY_SSL_KEYSTORE = FILE_CONFIG_KEY_PREFIX + "ssl.keyStore";

  String KEY_SSL_KEYSTORE_TYPE = FILE_CONFIG_KEY_PREFIX + "ssl.keyStoreType";

  String KEY_SSL_KEYSTORE_VALUE = FILE_CONFIG_KEY_PREFIX + "ssl.keyStoreValue";

  String KEY_SSL_CRL = FILE_CONFIG_KEY_PREFIX + "ssl.crl";

  String KEY_SSL_SSL_CUSTOM_CLASS = FILE_CONFIG_KEY_PREFIX + "ssl.sslCustomClass";

  // ###### ak / ak configuration ############### //
  String KEY_AK_SK_ENABLED = FILE_CONFIG_KEY_PREFIX + "credentials.enabled";

  String KEY_AK_SK_ACCESS_KEY = FILE_CONFIG_KEY_PREFIX + "credentials.accessKey";

  String KEY_AK_SK_SECRET_KEY = FILE_CONFIG_KEY_PREFIX + "credentials.secretKey";

  String KEY_AK_SK_CIPHER = FILE_CONFIG_KEY_PREFIX + "credentials.cipher";

  String KEY_AK_SK_PROJECT = FILE_CONFIG_KEY_PREFIX + "credentials.project";
}
