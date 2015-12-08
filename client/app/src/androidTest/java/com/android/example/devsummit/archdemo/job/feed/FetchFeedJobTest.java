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

import com.android.example.devsummit.archdemo.BaseTest;
import com.android.example.devsummit.archdemo.BuildConfig;
import com.android.example.devsummit.archdemo.api.ApiService;
import com.android.example.devsummit.archdemo.api.FeedResponse;
import com.android.example.devsummit.archdemo.event.LoggingBus;
import com.android.example.devsummit.archdemo.event.feed.FetchedFeedEvent;
import com.android.example.devsummit.archdemo.job.BaseJob;
import com.android.example.devsummit.archdemo.job.NetworkException;
import com.android.example.devsummit.archdemo.model.FeedModel;
import com.android.example.devsummit.archdemo.model.PostModel;
import com.android.example.devsummit.archdemo.model.UserModel;
import com.android.example.devsummit.archdemo.vo.Post;
import com.android.example.devsummit.archdemo.vo.User;
import com.path.android.jobqueue.RetryConstraint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import static com.android.example.devsummit.archdemo.util.TestUtil.createCall;
import static com.android.example.devsummit.archdemo.util.TestUtil.createDummyPost;
import static com.android.example.devsummit.archdemo.util.TestUtil.createDummyUser;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(AndroidJUnit4.class)
public class FetchFeedJobTest extends BaseTest {
    @Inject
    LoggingBus mLoggingBus;

    @Inject
    ApiService mApiService;

    @Inject
    UserModel mUserModel;

    @Inject
    PostModel mPostModel;

    @Inject
    FeedModel mFeedModel;

    @Before
    public void setup() {
        getTestComponent().inject(this);
    }

    @Test
    public void testRun() throws Throwable {
        FeedResponse response = new FeedResponse();
        List<User> users = Arrays.asList(createDummyUser(), createDummyUser());
        List<Post> posts = Arrays.asList(
                createDummyPost(users.get(0).getId()),
                createDummyPost(users.get(0).getId()),
                createDummyPost(users.get(1).getId())
        );
        posts.get(0).setCreated(10);
        posts.get(1).setCreated(11);
        posts.get(2).setCreated(12);
        response.setPosts(posts);
        response.setUsers(users);

        FetchFeedJob job = new FetchFeedJob(BaseJob.BACKGROUND, 101L);
        when(mApiService.userFeed(101L, 0L)).thenReturn(createCall(response));
        job.inject(getTestComponent());
        job.onRun();
        FetchedFeedEvent event = mLoggingBus.findEvent(FetchedFeedEvent.class);
        assertThat(event, notNullValue());
        assertThat(event.isSuccess(), is(true));
        assertThat(event.getUserId(), is(101L));
        // TODO Better to mock the models and verify the save calls so that this test wont
        // be effected by model failures.
        assertThat(mUserModel.load(users.get(0).getId()), notNullValue());
        assertThat(mUserModel.load(users.get(1).getId()), notNullValue());

        assertThat(mPostModel.load(posts.get(0).getId()), notNullValue());
        assertThat(mPostModel.load(posts.get(1).getId()), notNullValue());
        assertThat(mPostModel.load(posts.get(2).getId()), notNullValue());
        assertThat(mFeedModel.getLatestTimestamp(101L), is(12L));
    }

    @Test
    public void testCancel() throws Throwable {
        FetchFeedJob job = new FetchFeedJob(BaseJob.BACKGROUND, null);
        job.inject(getTestComponent());
        job.onCancel();
        FetchedFeedEvent event = mLoggingBus.findEvent(FetchedFeedEvent.class);
        assertThat(event, notNullValue());
        assertThat(event.isSuccess(), is(false));
        assertThat(event.getUserId(), nullValue());
    }

    @Test
    public void testCancelWithUser() throws Throwable {
        FetchFeedJob job = new FetchFeedJob(BaseJob.BACKGROUND, 101L);
        job.inject(getTestComponent());
        job.onCancel();
        FetchedFeedEvent event = mLoggingBus.findEvent(FetchedFeedEvent.class);
        assertThat(event, notNullValue());
        assertThat(event.isSuccess(), is(false));
        assertThat(event.getUserId(), is(101L));
    }

    @Test
    public void testFailure404() throws Throwable {
        FetchFeedJob job = new FetchFeedJob(BaseJob.BACKGROUND, null);
        when(mApiService.feed(0L)).thenReturn(createCall(404, new FeedResponse()));
        job.inject(getTestComponent());
        Throwable exception = safeRun(job);
        assertThat(exception, notNullValue());
        assertThat(exception, instanceOf(NetworkException.class));
        RetryConstraint retryConstraint = job.shouldReRunOnThrowable(exception, 1, 10);
        assertThat(retryConstraint.shouldRetry(), is(false));
    }

    @Test
    public void testFailure500() throws Throwable {
        FetchFeedJob job = new FetchFeedJob(BaseJob.BACKGROUND, null);
        when(mApiService.feed(0L)).thenReturn(createCall(500, new FeedResponse()));
        job.inject(getTestComponent());
        Throwable exception = safeRun(job);
        assertThat(exception, notNullValue());
        assertThat(exception, instanceOf(NetworkException.class));
        RetryConstraint retryConstraint = job.shouldReRunOnThrowable(exception, 1, 10);
        assertThat(retryConstraint.shouldRetry(), is(true));
    }

    @Test
    public void testDefaultQueryParam() {
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(long.class);
        FetchFeedJob job = new FetchFeedJob(BaseJob.BACKGROUND, null);
        job.inject(getTestComponent());
        //noinspection ThrowableResultOfMethodCallIgnored
        safeRun(job);
        verify(mApiService).feed(captor.capture());
        assertThat(captor.getValue(), is(0L));
    }

    @Test
    public void testSinceForUserFeed() {
        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(long.class);
        ArgumentCaptor<Long> sinceCaptor = ArgumentCaptor.forClass(long.class);

        FetchFeedJob job = new FetchFeedJob(BaseJob.BACKGROUND, 101L);
        FeedModel spyFeedModel = spy(mFeedModel);
        doReturn(123L).when(spyFeedModel).getLatestTimestamp(101L);
        job.inject(getTestComponent());
        job.mFeedModel = spyFeedModel;
        //noinspection ThrowableResultOfMethodCallIgnored
        safeRun(job);
        verify(mApiService).userFeed(userIdCaptor.capture(), sinceCaptor.capture());
        assertThat(userIdCaptor.getValue(), is(101L));
        assertThat(sinceCaptor.getValue(), is(123L));
    }

    @Test
    public void testSinceForCommonFeed() {
        ArgumentCaptor<Long> sinceCaptor = ArgumentCaptor.forClass(long.class);

        FetchFeedJob job = new FetchFeedJob(BaseJob.BACKGROUND, null);
        FeedModel spyFeedModel = spy(mFeedModel);
        doReturn(124L).when(spyFeedModel).getLatestTimestamp(null);
        job.inject(getTestComponent());
        job.mFeedModel = spyFeedModel;
        //noinspection ThrowableResultOfMethodCallIgnored
        safeRun(job);
        verify(mApiService).feed(sinceCaptor.capture());
        assertThat(sinceCaptor.getValue(), is(124L));
    }

    @Nullable
    private Throwable safeRun(FetchFeedJob job) {
        Throwable[] t = new Throwable[1];
        try {
            job.onRun();
        } catch (Throwable throwable) {
            t[0] = throwable;
        }
        return t[0];
    }
}
