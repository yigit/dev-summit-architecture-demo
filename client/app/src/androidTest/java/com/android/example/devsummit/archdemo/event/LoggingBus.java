/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.android.example.devsummit.archdemo.event;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

@SuppressWarnings("unchecked")
public class LoggingBus extends EventBus {
    private final List<Object> mEvents = new ArrayList<>();
    @Override
    public void post(Object event) {
        mEvents.add(event);
        super.post(event);
    }

    public void clear() {
        mEvents.clear();
    }

    public <T> T findEvent(Class<T> eventClass) {
        for (Object event : mEvents) {
            if (eventClass.isInstance(event)) {
                return (T) event;
            }
        }
        return null;
    }

    public <T> List<T> findEvents(Class<T> eventClass) {
        List<T> matching = new ArrayList<>();
        for (Object event : mEvents) {
            if (eventClass.isAssignableFrom(event.getClass())) {
                matching.add((T) event);
            }
        }
        return matching;
    }
}
