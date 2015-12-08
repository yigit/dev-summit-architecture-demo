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

package com.android.example.devsummit.archdemo.di.module;

import com.android.example.devsummit.archdemo.event.LoggingBus;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;

import static org.mockito.Mockito.*;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

@Singleton
@Module
public class TestApplicationModule {
    @Provides
    @Singleton
    public LoggingBus loggingBus(EventBus eventBus) {
        return (LoggingBus) eventBus;
    }
}
