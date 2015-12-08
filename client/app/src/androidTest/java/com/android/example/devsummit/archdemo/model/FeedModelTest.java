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

package com.android.example.devsummit.archdemo.model;

import com.android.example.devsummit.archdemo.BuildConfig;
import com.android.example.devsummit.archdemo.BaseTest;
import com.android.example.devsummit.archdemo.api.NewPostResponse;
import com.android.example.devsummit.archdemo.util.TestUtil;
import com.android.example.devsummit.archdemo.vo.FeedItem;
import com.android.example.devsummit.archdemo.vo.Post;
import com.android.example.devsummit.archdemo.vo.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FeedModelTest extends BaseTest {
    @Inject
    FeedModel mFeedModel;
    @Inject
    UserModel mUserModel;
    @Inject
    PostModel mPostModel;


    @Before
    public void setup() {
        getTestComponent().inject(this);
    }

    @Test
    public void loadFeed() {
        NewPostResponse response = new NewPostResponse();
        Post post = TestUtil.createDummyPost();
        User user = TestUtil.createDummyUser();
        post.setUserId(user.getId());
        response.setUser(user);
        response.setPost(post);
        mPostModel.save(post);
        mUserModel.save(user);

        assertThat(mUserModel.load(user.getId()), notNullValue());
        assertThat(mPostModel.load(post.getId()), notNullValue());

        List<FeedItem> feed = mFeedModel.loadFeed(0, user.getId());
        assertThat(feed, notNullValue());
        assertThat(feed.size(), is(1));
        FeedItem loaded = feed.get(0);

        assertThat(loaded.getUser().getId(), is(user.getId()));
        assertThat(loaded.getPost().getId(), is(post.getId()));
    }

    @Test
    public void timestampTracking() {
        assertThat(mFeedModel.getLatestTimestamp(1L), is(0L));
        assertThat(mFeedModel.getLatestTimestamp(null), is(0L));

        mFeedModel.saveFeedTimestamp(1, 10L);
        assertThat(mFeedModel.getLatestTimestamp(10L), is(1L));

        mFeedModel.saveFeedTimestamp(2, 20L);
        assertThat(mFeedModel.getLatestTimestamp(20L), is(2L));

        assertThat(mFeedModel.getLatestTimestamp(null), is(0L));

        mFeedModel.saveFeedTimestamp(3, null);
        assertThat(mFeedModel.getLatestTimestamp(null), is(3L));
    }

    @Test
    public void loadFeedOfUser() {
        long now = System.currentTimeMillis();
        Post p1 = TestUtil.createDummyPost();
        User u1 = TestUtil.createDummyUser();
        p1.setUserId(u1.getId());
        p1.setCreated(now);

        Post p2 = TestUtil.createDummyPost();
        User u2 = TestUtil.createDummyUser();
        p2.setUserId(u2.getId());
        p2.setCreated(now + 2);

        Post p3 = TestUtil.createDummyPost();
        p3.setUserId(u1.getId());
        p3.setCreated(now - 2);

        mPostModel.save(p1);
        mPostModel.save(p2);
        mPostModel.save(p3);
        mUserModel.save(u1);
        mUserModel.save(u2);

        assertThat(mFeedModel.loadFeed(0, null).size(), is(3));
        assertThat(mFeedModel.loadFeed(0, u1.getId()).size(), is(2));
        assertThat(mFeedModel.loadFeed(0, u2.getId()).size(), is(1));

        assertThat(mFeedModel.loadFeed(now, null).size(), is(1));
        assertThat(mFeedModel.loadFeed(now - 1, null).size(), is(2));
        assertThat(mFeedModel.loadFeed(now - 4, null).size(), is(3));

        assertThat(mFeedModel.loadFeed(now, u1.getId()).size(), is(0));
        assertThat(mFeedModel.loadFeed(now - 1, u1.getId()).size(), is(1));
        assertThat(mFeedModel.loadFeed(now - 4, u1.getId()).size(), is(2));
    }
}
