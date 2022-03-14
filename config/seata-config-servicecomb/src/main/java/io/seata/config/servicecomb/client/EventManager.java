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

package io.seata.config.servicecomb.client;

import com.google.common.eventbus.EventBus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
  private static volatile EventBus eventBus = new EventBus();
  private static List<String> eventBusNames = new ArrayList<>();

  public static void initEventBus() {
    if (eventBus == null) {
      synchronized (EventManager.class) {
        if (eventBus == null) {
          if (!eventBusNames.isEmpty()) {
            for (String className : eventBusNames) {
              try {
                Class<?> huaweiEventManagerClazz = Class.forName(className);
                Field busField = huaweiEventManagerClazz.getDeclaredField("eventBus");
                busField.setAccessible(true);
                eventBus = (EventBus) busField.get(null);
                break;
              } catch (Exception e) {
                //ignore
              }
            }
          }
          if (eventBus == null) {
            eventBus = new EventBus();
          }
        }
      }
    }
  }

  public static void addEventBusClass(String className) {
    eventBusNames.add(className);
  }

  public static EventBus getEventBus() {
    initEventBus();
    return eventBus;
  }

  public static void post(Object event) {
    initEventBus();
    eventBus.post(event);
  }

  public static void register(Object subscriber) {
    initEventBus();
    eventBus.register(subscriber);
  }

  public static void unregister(Object subscriber) {
    eventBus.unregister(subscriber);
  }
}
