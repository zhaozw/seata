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

package io.seata.config.servicecomb.client;

import com.google.common.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * on seata server side,create new EventBus instance,on client size,use the existing EventBus
 * @author zhaozhongwei22@163.com
 */
public class EventManager {
    private static final String HUAWEI_CLOUD_EVENT_MANAGER = "com.huaweicloud.common.event.EventManager";
    private static final String DUBBO_EVENT_MANAGER = "com.huaweicloud.dubbo.common.EventManager";
    private static final String SERVICECOMB_EVENT_MANAGER = "org.apache.servicecomb.foundation.common.event.EventManager";
    private static final String EVENT_BUS = "eventBus";
    private static EventBus eventBus;
    private static List<String> eventBusNames = new ArrayList<>();

    static {
        eventBusNames.add(HUAWEI_CLOUD_EVENT_MANAGER);
        eventBusNames.add(DUBBO_EVENT_MANAGER);
        eventBusNames.add(SERVICECOMB_EVENT_MANAGER);
        for (String className : eventBusNames) {
            try {
                Class<?> huaweiEventManagerClazz = Class.forName(className);
                Field busField = huaweiEventManagerClazz.getDeclaredField(EVENT_BUS);
                busField.setAccessible(true);
                eventBus = (EventBus)busField.get(null);
                break;
            } catch (Exception e) {
                // ignore
            }
        }
        if (eventBus == null) {
            eventBus = new EventBus();
        }
    }

    public static EventBus getEventBus() {
        return eventBus;
    }

    public static void post(Object event) {
        eventBus.post(event);
    }

    public static void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    public static void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }
}
