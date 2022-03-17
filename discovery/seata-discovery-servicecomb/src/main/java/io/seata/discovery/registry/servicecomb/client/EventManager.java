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

package io.seata.discovery.registry.servicecomb.client;

import com.google.common.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhaozhongwei22@163.com
 */
public class EventManager {
    private static EventBus eventBus;
    private static List<String> eventBusNames = new ArrayList<>();

    static {
        eventBusNames.add("com.huaweicloud.common.event.EventManager");
        eventBusNames.add("com.huaweicloud.dubbo.common.EventManager");
        eventBusNames.add("org.apache.servicecomb.foundation.common.event.EventManager");
        for (String className : eventBusNames) {
            try {
                Class<?> huaweiEventManagerClazz = Class.forName(className);
                Field busField = huaweiEventManagerClazz.getDeclaredField("eventBus");
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
