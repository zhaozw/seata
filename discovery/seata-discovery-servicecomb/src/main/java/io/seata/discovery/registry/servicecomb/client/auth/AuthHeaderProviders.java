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

package io.seata.discovery.registry.servicecomb.client.auth;

import io.seata.discovery.registry.servicecomb.client.CommonConfiguration;
import io.seata.discovery.registry.servicecomb.client.StringUtils;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class AuthHeaderProviders {

  public static RequestAuthHeaderProvider getRequestAuthHeaderProvider(Properties environment) {
    List<AuthHeaderProvider> authHeaderProviders = new ArrayList<>();
    authHeaderProviders.add(createAkSkRequestAuthHeaderProvider(environment));
    //authHeaderProviders.add(new RBACRequestAuthHeaderProvider(commonConfiguration, environment));
    return getRequestAuthHeaderProvider(authHeaderProviders);
  }
  public static HttpConfiguration.SSLProperties createSSLProperties(Properties environment) {
    HttpConfiguration.SSLProperties sslProperties = new HttpConfiguration.SSLProperties();
    sslProperties.setEnabled(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SSL_ENABLED, "false")));
    if (sslProperties.isEnabled()) {
      SSLOption option = new SSLOption();
      option.setEngine(environment.getProperty(CommonConfiguration.KEY_SSL_ENGINE, "jdk"));
      option.setProtocols(environment.getProperty(CommonConfiguration.KEY_SSL_PROTOCOLS, "TLSv1.2"));
      option.setCiphers(environment.getProperty(CommonConfiguration.KEY_SSL_CIPHERS, CommonConfiguration.DEFAULT_CIPHERS));
      option.setAuthPeer(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SSL_AUTH_PEER, "false")));
      option.setCheckCNHost(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SSL_CHECKCN_HOST, "false")));
      option.setCheckCNWhite(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SSL_CHECKCN_WHITE, "false")));
      option.setCheckCNWhiteFile(environment.getProperty(CommonConfiguration.KEY_SSL_CHECKCN_WHITE_FILE, "white.list"));
      option.setAllowRenegociate(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_SSL_ALLOW_RENEGOTIATE, "false")));
      option.setStorePath(environment.getProperty(CommonConfiguration.KEY_SSL_STORE_PATH, "internal"));
      option.setKeyStore(environment.getProperty(CommonConfiguration.KEY_SSL_KEYSTORE, "server.p12"));
      option.setKeyStoreType(environment.getProperty(CommonConfiguration.KEY_SSL_KEYSTORE_TYPE, "PKCS12"));
      option.setKeyStoreValue(environment.getProperty(CommonConfiguration.KEY_SSL_KEYSTORE_VALUE, "keyStoreValue"));
      option.setTrustStore(environment.getProperty(CommonConfiguration.KEY_SSL_TRUST_STORE, "trust.jks"));
      option.setTrustStoreType(environment.getProperty(CommonConfiguration.KEY_SSL_TRUST_STORE_TYPE, "JKS"));
      option.setTrustStoreValue(environment.getProperty(CommonConfiguration.KEY_SSL_TRUST_STORE_VALUE, "trustStoreValue"));
      option.setCrl(environment.getProperty(CommonConfiguration.KEY_SSL_CRL, "revoke.crl"));

      SSLCustom sslCustom = SSLCustom.createSSLCustom(environment.getProperty(CommonConfiguration.KEY_SSL_SSL_CUSTOM_CLASS, ""));
      sslProperties.setSslOption(option);
      sslProperties.setSslCustom(sslCustom);
    }
    return sslProperties;
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(authHeaderProvider -> headers.putAll(authHeaderProvider.authHeaders()));
      return headers;
    };
  }

  private static AkSkRequestAuthHeaderProvider createAkSkRequestAuthHeaderProvider(Properties environment) {
    AkSkRequestAuthHeaderProvider requestAuthHeaderProvider = new AkSkRequestAuthHeaderProvider();
    requestAuthHeaderProvider.setEnabled(Boolean.parseBoolean(environment.getProperty(CommonConfiguration.KEY_AK_SK_ENABLED, "false")));
    requestAuthHeaderProvider.setAccessKey(environment.getProperty(CommonConfiguration.KEY_AK_SK_ACCESS_KEY, ""));
    requestAuthHeaderProvider.setSecretKey(environment.getProperty(CommonConfiguration.KEY_AK_SK_SECRET_KEY, ""));
    requestAuthHeaderProvider.setCipher(environment.getProperty(CommonConfiguration.KEY_AK_SK_CIPHER, ""));
    requestAuthHeaderProvider.setProject(safeGetProject(environment.getProperty(CommonConfiguration.KEY_AK_SK_PROJECT, "")));
    return requestAuthHeaderProvider;
  }

  private static String safeGetProject(String project) {
    if (StringUtils.isEmpty(project)) {
      return project;
    }
    try {
      return URLEncoder.encode(project, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return project;
    }
  }
}
