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

package com.android.example.devsummit.archdemo.job.feed;

import com.android.example.devsummit.archdemo.api.ApiService;
import com.android.example.devsummit.archdemo.api.FeedResponse;
import com.android.example.devsummit.archdemo.di.component.AppComponent;
import com.android.example.devsummit.archdemo.event.feed.FetchedFeedEvent;
import com.android.example.devsummit.archdemo.job.BaseJob;
import com.android.example.devsummit.archdemo.job.NetworkException;
import com.android.example.devsummit.archdemo.model.FeedModel;
import com.android.example.devsummit.archdemo.model.PostModel;
import com.android.example.devsummit.archdemo.model.UserModel;
import com.android.example.devsummit.archdemo.vo.Post;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Response;

public class FetchFeedJob extends BaseJob {

    private static final String GROUP = "FetchFeedJob";
    private final Long mUserId;
    @Inject
    transient FeedModel mFeedModel;
    @Inject
    transient EventBus mEventBus;
    @Inject
    transient UserModel mUserModel;
    @Inject
    transient PostModel mPostModel;
    @Inject
    transient ApiService mApiService;

    public FetchFeedJob(@Priority int priority, @Nullable Long userId) {
        super(new Params(priority).addTags(GROUP).requireNetwork());
        mUserId = userId;
    }

    @Override
    public void inject(AppComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRun() throws Throwable {
        long timestamp = mFeedModel.getLatestTimestamp(mUserId);
        final Call<FeedResponse> feed;
        if (mUserId == null) {
            feed = mApiService.feed(timestamp);
        } else {
            feed = mApiService.userFeed(mUserId, timestamp);
        }
        Response<FeedResponse> response = feed.execute();
        if (response.isSuccess()) {
            Post oldest = handleResponse(response.body());
            mEventBus.post(new FetchedFeedEvent(true, mUserId, oldest));
        } else {
            throw new NetworkException(response.code());
        }
    }

    @Nullable
    private Post handleResponse(FeedResponse body) {
        // We could put these two into a transaction but it is OK to save a user even if we could
        // not save their post so we don't care.
        if (body.getUsers() != null) {
            mUserModel.saveAll(body.getUsers());
        }
        Post oldest = null;
        if (body.getPosts() != null) {
            mPostModel.saveAll(body.getPosts());
            long since = 0;
            for (Post post : body.getPosts()) {
                if (post.getCreated() > since) {
                    since = post.getCreated();
                }
                if (oldest == null || oldest.getCreated() > post.getCreated()) {
                    oldest = post;
                }
            }
            if (since > 0) {
                mFeedModel.saveFeedTimestamp(since, mUserId);
            }
        }
        return oldest;
    }

    @Override
    public RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
            int maxRunCount) {
        if (shouldRetry(throwable)) {
            return RetryConstraint.createExponentialBackoff(runCount, 1000);
        }
        return RetryConstraint.CANCEL;
    }

    @Override
    protected int getRetryLimit() {
        return 2;
    }

    @Override
    protected void onCancel() {
        mEventBus.post(new FetchedFeedEvent(false, mUserId, null));
    }
}
