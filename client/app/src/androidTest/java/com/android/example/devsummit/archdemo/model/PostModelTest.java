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

import com.android.example.devsummit.archdemo.BaseTest;
import com.android.example.devsummit.archdemo.BuildConfig;
import com.android.example.devsummit.archdemo.util.TestUtil;
import com.android.example.devsummit.archdemo.util.ValidationFailedException;
import com.android.example.devsummit.archdemo.vo.Post;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(AndroidJUnit4.class)
public class PostModelTest extends BaseTest {

    @Inject
    public PostModel mPostModel;

    @Before
    public  void setup() {
        getTestComponent().inject(this);
    }

    @Test
    public void addLoad() {
        Post post = new Post();
        post.setId(1);
        post.setText("bla");
        post.setClientId("abc");
        post.setUserId(1);
        post.setCreated(System.currentTimeMillis());
        mPostModel.save(post);
        Post loaded = mPostModel.load(1);
        assertThat(loaded, notNullValue());
        assertThat(loaded.getId(), is(1L));
        assertThat(loaded.getText(), is("bla"));
        assertThat(loaded.getClientId(), is("abc"));

        // try to re add
        Post post2 = new Post();
        post2.setClientId("abc");
        post2.setText("xxx");
        post2.setUserId(1);
        post2.setCreated(System.currentTimeMillis());
        mPostModel.save(post2);

        List<Post> posts = mPostModel.loadPostsSince(post2.getCreated() - 1);
        assertThat(posts.size(), is(1));

        Post updatedPost = posts.get(0);
        assertThat(updatedPost.getId(), is(1L));
        assertThat(updatedPost.getText(), is("xxx"));
    }

    @Test(expected = ValidationFailedException.class)
    public void insertInvalidPost() {
        Post p = new Post();
        p.setUserId(-1);
        mPostModel.save(p);
    }

    @Test
    public void loadAll() {
        for (int i = 0; i < 10; i++) {
            mPostModel.save(TestUtil.createDummyPost());
        }
        assertThat(mPostModel.loadPostsSince(0).size(), is(10));
    }

    @Test
    public void loadWithTimestamp() {
        long createdStart = System.currentTimeMillis();
        Post p1 = TestUtil.createDummyPost();
        p1.setCreated(createdStart - 10);
        Post p2 = TestUtil.createDummyPost();
        p2.setCreated(createdStart);
        Post p3 = TestUtil.createDummyPost();
        p3.setCreated(createdStart + 10);

        mPostModel.save(p1);
        mPostModel.save(p2);
        mPostModel.save(p3);

        assertThat(mPostModel.loadPostsSince(createdStart - 11).size(), is(3));
        assertThat(mPostModel.loadPostsSince(createdStart - 10).size(), is(2));
        assertThat(mPostModel.loadPostsSince(createdStart + 10).size(), is(0));
    }

    @Test
    public void loadPostsOfUser() {
        long created = System.nanoTime();
        Post p1 = TestUtil.createDummyPost();
        Post p2 = TestUtil.createDummyPost();
        p1.setCreated(created);
        p1.setUserId(1);
        p2.setCreated(created);
        p2.setUserId(2);
        mPostModel.save(p1);
        mPostModel.save(p2);

        assertThat(mPostModel.loadPostsSince(0).size(), is(2));
        assertThat(mPostModel.loadPostsOfUser(1, created - 1).size(), is(1));
        assertThat(mPostModel.loadPostsOfUser(2, created - 1).size(), is(1));
        assertThat(mPostModel.loadPostsOfUser(3, 0).size(), is(0));
        assertThat(mPostModel.loadPostsOfUser(2, created).size(), is(0));

        p1.setUserId(2);
        mPostModel.save(p1);

        assertThat(mPostModel.loadPostsOfUser(1, 0).size(), is(0));
        assertThat(mPostModel.loadPostsOfUser(2, 0).size(), is(2));
    }

    @Test
    public void delete() {
        Post post = TestUtil.createDummyPost();
        final long id = post.getId();
        mPostModel.save(post);
        mPostModel.delete(post);

        assertThat(mPostModel.load(id), nullValue());
    }

    @Test
    public void loadByClientIds() {
        Post post = TestUtil.createDummyPost();
        String clientId = post.getClientId();
        long userId = post.getUserId();
        mPostModel.save(post);
        Post loaded = mPostModel.loadByClientIdAndUserId(clientId, userId);
        assertThat(loaded, notNullValue());
        assertThat(loaded.getUserId(), is(userId));
        assertThat(loaded.getClientId(), is(clientId));
    }
}