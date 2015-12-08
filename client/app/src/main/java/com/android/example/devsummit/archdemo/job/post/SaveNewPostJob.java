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

package com.android.example.devsummit.archdemo.job.post;

import com.android.example.devsummit.archdemo.api.ApiService;
import com.android.example.devsummit.archdemo.api.NewPostResponse;
import com.android.example.devsummit.archdemo.config.DemoConfig;
import com.android.example.devsummit.archdemo.di.component.AppComponent;
import com.android.example.devsummit.archdemo.event.post.DeletePostEvent;
import com.android.example.devsummit.archdemo.event.post.NewPostEvent;
import com.android.example.devsummit.archdemo.event.post.UpdatedPostEvent;
import com.android.example.devsummit.archdemo.job.BaseJob;
import com.android.example.devsummit.archdemo.job.NetworkException;
import com.android.example.devsummit.archdemo.model.FeedModel;
import com.android.example.devsummit.archdemo.model.PostModel;
import com.android.example.devsummit.archdemo.model.UserModel;
import com.android.example.devsummit.archdemo.util.L;
import com.android.example.devsummit.archdemo.vo.Post;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.RetryConstraint;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit.Response;

public class SaveNewPostJob extends BaseJob {

    private static final String GROUP = "new_post";

    private final String mText;

    private final String mClientId;

    private final long mUserId;

    @Inject
    transient ApiService mApiService;

    @Inject
    transient EventBus mEventBus;

    @Inject
    transient FeedModel mFeedModel;

    @Inject
    transient PostModel mPostModel;

    @Inject
    transient UserModel mUserModel;

    @Inject
    transient DemoConfig mDemoConfig;

    public SaveNewPostJob(String text, String clientId, long userId) {
        super(new Params(BACKGROUND).groupBy(GROUP).requireNetwork().persist());
        mText = text;
        mClientId = clientId;
        mUserId = userId;
    }

    @Override
    public void inject(AppComponent appComponent) {
        super.inject(appComponent);
        appComponent.inject(this);
    }

    @Override
    public void onAdded() {
        Post post = new Post();
        post.setText(mText);
        post.setId(mFeedModel.generateIdForNewLocalPost());
        post.setClientId(mClientId);
        post.setUserId(mUserId);
        post.setPending(true);
        // make sure whatever time we put here is greater / eq to last known time in database.
        // this will work around issues related to client's time.
        // this time is temporary anyways as it will be overriden when it is synched to server
        long feedTs = mFeedModel.getLatestTimestamp(null);
        long now = System.currentTimeMillis();
        post.setCreated(Math.max(feedTs, now) + 1);
        L.d("assigned timestamp %s to the post", post.getCreated());
        mPostModel.save(post);
        mEventBus.post(new NewPostEvent(post));
    }

    @Override
    public void onRun() throws Throwable {
        Post post = mPostModel.loadByClientIdAndUserId(mClientId, mUserId);
        if (post != null && !post.isPending()) {
            // looks like post probably arrived from somewhere else. Good Job!
            mEventBus.post(new UpdatedPostEvent(post));
            return;
        }
        Response<NewPostResponse> response = mApiService.sendPost(mText, mClientId, mUserId)
                .execute();
        if (response.isSuccess()) {
            NewPostResponse body = response.body();
            body.getPost().setPending(false);
            mPostModel.save(body.getPost());
            mUserModel.save(body.getUser());
            mEventBus.post(new UpdatedPostEvent(body.getPost()));
        } else {
            throw new NetworkException(response.code());
        }
    }

    @Override
    protected int getRetryLimit() {
        return mDemoConfig.getNewPostRetryCount();
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount,
            int maxRunCount) {
        if (shouldRetry(throwable)) {
            // For the purposes of the demo, just back off 250 ms.
            RetryConstraint constraint = RetryConstraint
                    .createExponentialBackoff(runCount, 250);
            constraint.setApplyNewDelayToGroup(true);
            return constraint;
        }
        return RetryConstraint.CANCEL;
    }

    @Override
    protected void onCancel() {
        Post post = mPostModel.loadByClientIdAndUserId(mClientId, mUserId);
        if (post != null) {
            mPostModel.delete(post);
        }
        mEventBus.post(new DeletePostEvent(true, mText, post));
    }
}
