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

package io.seata.config.servicecomb;


public interface SeataServicecombKeys {
  String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,"
          + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

  String DEFAULT_PROJECT = "default";
  String FILE_ROOT_REGISTRY = "config";
  String FILE_CONFIG_SPLIT_CHAR = ".";
  String REGISTRY_TYPE = "servicecomb";

  String FILE_CONFIG_KEY_PREFIX = FILE_ROOT_REGISTRY + FILE_CONFIG_SPLIT_CHAR + REGISTRY_TYPE
          + FILE_CONFIG_SPLIT_CHAR;

  String FILE_CONFIG_KEY_PREFIX2 = "registry" + FILE_CONFIG_SPLIT_CHAR + REGISTRY_TYPE
          + FILE_CONFIG_SPLIT_CHAR;

  // ###### service configuration ############### //
  String KEY_SERVICE_PROJECT = FILE_CONFIG_KEY_PREFIX2 + "project";

  String KEY_SERVICE_APPLICATION = FILE_CONFIG_KEY_PREFIX2 + "application";

  String KEY_SERVICE_NAME = FILE_CONFIG_KEY_PREFIX2 + "name";

  String KEY_SERVICE_VERSION = FILE_CONFIG_KEY_PREFIX2 + "version";

  String KEY_SERVICE_ENVIRONMENT = FILE_CONFIG_KEY_PREFIX2 + "environment";

  /**/
  // ###### config center configuration ############### //
  String KEY_CONFIG_ADDRESSTYPE = FILE_CONFIG_KEY_PREFIX + "type";

  String KEY_CONFIG_FILESOURCE = FILE_CONFIG_KEY_PREFIX + "fileSource";

  String KEY_CONFIG_ADDRESS = FILE_CONFIG_KEY_PREFIX + "address";

  // ###### kie config center polling configuration############### //
  String KEY_SERVICE_ENABLELONGPOLLING = FILE_CONFIG_KEY_PREFIX2 + "enableLongPolling";

  String KEY_SERVICE_POLLINGWAITSEC = FILE_CONFIG_KEY_PREFIX2 + "pollingWaitInSeconds";

  // ###### kie configuration############### //

  String KEY_SERVICE_KIE_CUSTOMLABEL = FILE_CONFIG_KEY_PREFIX + "customLabel";

  String KEY_SERVICE_KIE_CUSTOMLABELVALUE = FILE_CONFIG_KEY_PREFIX + "customLabelValue";

  String KEY_SERVICE_KIE_FRISTPULLREQUIRED = FILE_CONFIG_KEY_PREFIX + "firstPullRequired";

  String KEY_SERVICE_KIE_ENABLEAPPCONFIG = FILE_CONFIG_KEY_PREFIX + "enableAppConfig";

  String KEY_SERVICE_KIE_ENABLECUSTOMCONFIG = FILE_CONFIG_KEY_PREFIX + "enableCustomConfig";

  String KEY_SERVICE_KIE_ENABLESERVICECONFIG = FILE_CONFIG_KEY_PREFIX + "enableServiceConfig";

  // ###### ssl configuration ############### //
  String KEY_SSL_ENABLED = FILE_CONFIG_KEY_PREFIX2 + "ssl.enabled";

  String KEY_SSL_ENGINE = FILE_CONFIG_KEY_PREFIX2 + "ssl.engine";

  String KEY_SSL_PROTOCOLS = FILE_CONFIG_KEY_PREFIX2 + "ssl.protocols";

  String KEY_SSL_CIPHERS = FILE_CONFIG_KEY_PREFIX2 + "ssl.ciphers";

  String KEY_SSL_AUTH_PEER = FILE_CONFIG_KEY_PREFIX2 + "ssl.authPeer";

  String KEY_SSL_CHECKCN_HOST = FILE_CONFIG_KEY_PREFIX2 + "ssl.checkCNHost";

  String KEY_SSL_CHECKCN_WHITE = FILE_CONFIG_KEY_PREFIX2 + "ssl.checkCNWhite";

  String KEY_SSL_CHECKCN_WHITE_FILE = FILE_CONFIG_KEY_PREFIX2 + "ssl.checkCNWhiteFile";

  String KEY_SSL_ALLOW_RENEGOTIATE = FILE_CONFIG_KEY_PREFIX2 + "ssl.allowRenegotiate";

  String KEY_SSL_STORE_PATH = FILE_CONFIG_KEY_PREFIX2 + "ssl.storePath";

  String KEY_SSL_TRUST_STORE = FILE_CONFIG_KEY_PREFIX2 + "ssl.trustStore";

  String KEY_SSL_TRUST_STORE_TYPE = FILE_CONFIG_KEY_PREFIX2 + "ssl.trustStoreType";

  String KEY_SSL_TRUST_STORE_VALUE = FILE_CONFIG_KEY_PREFIX2 + "ssl.trustStoreValue";

  String KEY_SSL_KEYSTORE = FILE_CONFIG_KEY_PREFIX2 + "ssl.keyStore";

  String KEY_SSL_KEYSTORE_TYPE = FILE_CONFIG_KEY_PREFIX2 + "ssl.keyStoreType";

  String KEY_SSL_KEYSTORE_VALUE = FILE_CONFIG_KEY_PREFIX2 + "ssl.keyStoreValue";

  String KEY_SSL_CRL = FILE_CONFIG_KEY_PREFIX2 + "ssl.crl";

  String KEY_SSL_SSL_CUSTOM_CLASS = FILE_CONFIG_KEY_PREFIX2 + "ssl.sslCustomClass";

  // ###### ak / ak configuration ############### //
  String KEY_AK_SK_ENABLED = FILE_CONFIG_KEY_PREFIX2 + "credentials.enabled";

  String KEY_AK_SK_ACCESS_KEY = FILE_CONFIG_KEY_PREFIX2 + "credentials.accessKey";

  String KEY_AK_SK_SECRET_KEY = FILE_CONFIG_KEY_PREFIX2 + "credentials.secretKey";

  String KEY_AK_SK_CIPHER = FILE_CONFIG_KEY_PREFIX2 + "credentials.cipher";

  String KEY_AK_SK_PROJECT = FILE_CONFIG_KEY_PREFIX2 + "credentials.project";
}
