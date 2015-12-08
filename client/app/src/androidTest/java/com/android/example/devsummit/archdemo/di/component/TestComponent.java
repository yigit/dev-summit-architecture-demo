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

package com.android.example.devsummit.archdemo.di.component;

import com.android.example.devsummit.archdemo.di.module.TestApiServiceModule;
import com.android.example.devsummit.archdemo.job.feed.FetchFeedJobTest;
import com.android.example.devsummit.archdemo.model.FeedModelTest;
import com.android.example.devsummit.archdemo.model.PostModelTest;
import com.android.example.devsummit.archdemo.di.module.ApplicationModule;
import com.android.example.devsummit.archdemo.di.module.TestApplicationModule;
import com.android.example.devsummit.archdemo.event.LoggingBus;
import com.android.example.devsummit.archdemo.model.UserModelTest;

import android.database.sqlite.SQLiteDatabase;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {TestApplicationModule.class, ApplicationModule.class,
        TestApiServiceModule.class})
public interface TestComponent extends AppComponent {

    void inject(PostModelTest postModelTest);

    LoggingBus loggingBus();

    SQLiteDatabase database();

    void inject(FeedModelTest feedModelTest);

    void inject(UserModelTest userModelTest);

    void inject(FetchFeedJobTest fetchFeedJobTest);
}
