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
package io.seata.spring.boot.autoconfigure.properties.registry;

import io.seata.spring.boot.autoconfigure.properties.config.ConfigServicecombProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import static io.seata.spring.boot.autoconfigure.StarterConstants.REGISTRY_SERVICECOMB_PREFIX;

/**
 * @author xingfudeshi@gmail.com
 */
@Component
@ConfigurationProperties(prefix = REGISTRY_SERVICECOMB_PREFIX)
public class RegistryServicecombProperties {
    private String address;
    private String project;
    private String name;
    private String application;
    private String initialStatus;
    private String enableLongPolling;
    private String pollingWaitInSeconds;
    private String environment;
    private String enableversionAppConfig;
    private String version;

    public String getVersion() {
        return version;
    }

    public RegistryServicecombProperties setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getInitialStatus() {
        return initialStatus;
    }

    public RegistryServicecombProperties setInitialStatus(String initialStatus) {
        this.initialStatus = initialStatus;
        return this;
    }

    public String getEnableLongPolling() {
        return enableLongPolling;
    }

    public RegistryServicecombProperties setEnableLongPolling(String enableLongPolling) {
        this.enableLongPolling = enableLongPolling;
        return this;
    }

    public String getPollingWaitInSeconds() {
        return pollingWaitInSeconds;
    }

    public RegistryServicecombProperties setPollingWaitInSeconds(String pollingWaitInSeconds) {
        this.pollingWaitInSeconds = pollingWaitInSeconds;
        return this;
    }

    public String getEnvironment() {
        return environment;
    }

    public RegistryServicecombProperties setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public String getEnableversionAppConfig() {
        return enableversionAppConfig;
    }

    public RegistryServicecombProperties setEnableversionAppConfig(String enableversionAppConfig) {
        this.enableversionAppConfig = enableversionAppConfig;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public RegistryServicecombProperties setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getProject() {
        return project;
    }

    public RegistryServicecombProperties setProject(String project) {
        this.project = project;
        return this;
    }

    public String getName() {
        return name;
    }

    public RegistryServicecombProperties setName(String name) {
        this.name = name;
        return this;
    }

    public String getApplication() {
        return application;
    }

    public RegistryServicecombProperties setApplication(String application) {
        this.application = application;
        return this;
    }

}
