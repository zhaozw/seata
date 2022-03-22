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

package io.seata.config.servicecomb.client.auth;

import io.seata.config.servicecomb.client.CommonConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.foundation.ssl.SSLCustom;
import org.apache.servicecomb.foundation.ssl.SSLOption;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author zhaozhongwei22@163.com
 */
public class AuthHeaderProviders {

    public static RequestAuthHeaderProvider getRequestAuthHeaderProvider(Properties properties) {
        List<AuthHeaderProvider> authHeaderProviders = new ArrayList<>();
        return getRequestAuthHeaderProvider(authHeaderProviders);
    }

    public static HttpConfiguration.SSLProperties createSslProperties(Properties properties) {

        HttpConfiguration.SSLProperties sslProperties = new HttpConfiguration.SSLProperties();
        sslProperties
            .setEnabled(Boolean.parseBoolean(properties.getProperty(CommonConfiguration.KEY_SSL_ENABLED, CommonConfiguration.FALSE)));
        if (sslProperties.isEnabled()) {
            SSLOption option = new SSLOption();
            option.setEngine(properties.getProperty(CommonConfiguration.KEY_SSL_ENGINE, CommonConfiguration.JDK));
            option.setProtocols(properties.getProperty(CommonConfiguration.KEY_SSL_PROTOCOLS, CommonConfiguration.TLS));
            option.setCiphers(
                properties.getProperty(CommonConfiguration.KEY_SSL_CIPHERS, CommonConfiguration.DEFAULT_CIPHERS));
            option.setAuthPeer(
                Boolean.parseBoolean(properties.getProperty(CommonConfiguration.KEY_SSL_AUTH_PEER, CommonConfiguration.FALSE)));
            option.setCheckCNHost(
                Boolean.parseBoolean(properties.getProperty(CommonConfiguration.KEY_SSL_CHECKCN_HOST, CommonConfiguration.FALSE)));
            option.setCheckCNWhite(
                Boolean.parseBoolean(properties.getProperty(CommonConfiguration.KEY_SSL_CHECKCN_WHITE, CommonConfiguration.FALSE)));
            option.setCheckCNWhiteFile(
                properties.getProperty(CommonConfiguration.KEY_SSL_CHECKCN_WHITE_FILE, CommonConfiguration.EMPTY));
            option.setAllowRenegociate(
                Boolean.parseBoolean(properties.getProperty(CommonConfiguration.KEY_SSL_ALLOW_RENEGOTIATE, CommonConfiguration.FALSE)));
            option.setStorePath(properties.getProperty(CommonConfiguration.KEY_SSL_STORE_PATH, CommonConfiguration.INTERNAL));
            option.setKeyStore(properties.getProperty(CommonConfiguration.KEY_SSL_KEYSTORE, CommonConfiguration.EMPTY));
            option.setKeyStoreType(properties.getProperty(CommonConfiguration.KEY_SSL_KEYSTORE_TYPE, CommonConfiguration.PKCS12));
            option
                .setKeyStoreValue(properties.getProperty(CommonConfiguration.KEY_SSL_KEYSTORE_VALUE, CommonConfiguration.EMPTY));
            option.setTrustStore(properties.getProperty(CommonConfiguration.KEY_SSL_TRUST_STORE, CommonConfiguration.EMPTY));
            option.setTrustStoreType(properties.getProperty(CommonConfiguration.KEY_SSL_TRUST_STORE_TYPE, CommonConfiguration.EMPTY));
            option.setTrustStoreValue(
                properties.getProperty(CommonConfiguration.KEY_SSL_TRUST_STORE_VALUE, CommonConfiguration.EMPTY));
            option.setCrl(properties.getProperty(CommonConfiguration.KEY_SSL_CRL, CommonConfiguration.EMPTY));

            SSLCustom sslCustom =
                SSLCustom.createSSLCustom(properties.getProperty(CommonConfiguration.KEY_SSL_SSL_CUSTOM_CLASS, CommonConfiguration.EMPTY));
            sslProperties.setSslOption(option);
            sslProperties.setSslCustom(sslCustom);
        }
        return sslProperties;
    }

    public static RequestAuthHeaderProvider
        getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
        return signRequest -> {
            Map<String, String> headers = new HashMap<>(0);
            authHeaderProviders.forEach(authHeaderProvider -> headers.putAll(authHeaderProvider.authHeaders()));
            return headers;
        };
    }

    private static String safeGetProject(String project) {
        if (StringUtils.isEmpty(project)) {
            return project;
        }
        try {
            return URLEncoder.encode(project, CommonConfiguration.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return project;
        }
    }
}
