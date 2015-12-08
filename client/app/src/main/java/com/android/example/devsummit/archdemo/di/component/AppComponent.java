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

import com.android.example.devsummit.archdemo.api.ApiModule;
import com.android.example.devsummit.archdemo.api.ApiService;
import com.android.example.devsummit.archdemo.config.DemoConfig;
import com.android.example.devsummit.archdemo.controller.FeedController;
import com.android.example.devsummit.archdemo.di.module.ApplicationModule;
import com.android.example.devsummit.archdemo.job.feed.FetchFeedJob;
import com.android.example.devsummit.archdemo.job.post.SaveNewPostJob;
import com.android.example.devsummit.archdemo.model.FeedModel;
import com.android.example.devsummit.archdemo.model.PostModel;
import com.android.example.devsummit.archdemo.model.UserModel;
import com.path.android.jobqueue.JobManager;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

import javax.inject.Singleton;

import dagger.Component;
import de.greenrobot.event.EventBus;

@Singleton
@Component(modules = {ApplicationModule.class, ApiModule.class})
public interface AppComponent {

    JobManager jobManager();

    UserModel userModel();

    PostModel postModel();

    EventBus eventBus();

    ApiService apiService();

    FeedModel feedModel();

    FeedController feedController();

    Context appContext();

    DemoConfig demoConfig();

    NotificationManagerCompat notificationManagerCompat();

    void inject(FeedController feedController);

    void inject(FeedModel feedModel);

    void inject(FetchFeedJob fetchFeedJob);

    void inject(SaveNewPostJob saveNewPostJob);
}
