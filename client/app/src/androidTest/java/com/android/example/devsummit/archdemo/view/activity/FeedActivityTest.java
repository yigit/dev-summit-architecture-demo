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

package com.android.example.devsummit.archdemo.view.activity;

import com.android.example.devsummit.archdemo.App;
import com.android.example.devsummit.archdemo.R;
import com.android.example.devsummit.archdemo.controller.FeedController;
import com.android.example.devsummit.archdemo.di.component.AppComponent;
import com.android.example.devsummit.archdemo.event.feed.FetchedFeedEvent;
import com.android.example.devsummit.archdemo.model.FeedModel;
import com.android.example.devsummit.archdemo.util.DelegatingAppComponent;
import com.android.example.devsummit.archdemo.util.TestUtil;
import com.android.example.devsummit.archdemo.vo.FeedItem;
import com.android.example.devsummit.archdemo.vo.Post;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class FeedActivityTest extends ActivityInstrumentationTestCase2<FeedActivity> {
    private AppComponent mComponent;
    private boolean mCreatedActivity = false;
    private IdlingResource mIdlingResource;
    @Override
    public FeedActivity getActivity() {
        FeedActivity result = super.getActivity();
        mCreatedActivity = true;
        return result;
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            Espresso.unregisterIdlingResources(mIdlingResource);
            mIdlingResource = null;
        }
    }

    @Before
    public void registerIdlingResource() {
        mIdlingResource = new IdlingResource() {
            ResourceCallback mResourceCallback;

            @Override
            public String getName() {
                return "RecyclerView";
            }

            @Override
            public boolean isIdleNow() {
                boolean idle = !mCreatedActivity ||
                        !getActivity().mBinding.list.hasPendingAdapterUpdates();
                if (idle && mResourceCallback != null) {
                    mResourceCallback.onTransitionToIdle();
                }
                return idle;
            }

            @Override
            public void registerIdleTransitionCallback(ResourceCallback callback) {
                mResourceCallback = callback;
            }
        };
        Espresso.registerIdlingResources(mIdlingResource);
    }

    public FeedActivityTest() {
        super(FeedActivity.class);
        App app = (App) InstrumentationRegistry.getTargetContext().getApplicationContext();
        mComponent = spy(new DelegatingAppComponent(TestUtil.prepare(app)));
        app.setAppComponent(mComponent);
    }

    @Before
    public void setup() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void loadFeed() {
        FeedModel mockFeedModel = mock(FeedModel.class);
        when(mComponent.feedModel()).thenReturn(mockFeedModel);
        List<FeedItem> items = Collections.singletonList(TestUtil.createDummyFeedItem());
        when(mockFeedModel.loadFeed(0L, null)).thenReturn(items);
        getActivity();
        onView(withId(R.id.user_name)).check(matches(withText(items.get(0).getUser().getName())));
        onView(withId(R.id.post_text)).check(matches(withText(items.get(0).getPost().getText())));
    }

    @Test
    public void dontPostWithEmptyTextView() {
        FeedController mockFeedController = mock(FeedController.class);
        doNothing().when(mockFeedController).sendPostAsync(anyString());
        when(mComponent.feedController()).thenReturn(mockFeedController);
        getActivity();
        onView(withId(R.id.fab)).perform(ViewActions.click());
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockFeedController, never()).sendPostAsync(captor.capture());
    }

    @Test
    public void postItem() throws InterruptedException {
        FeedController mockFeedController = mock(FeedController.class);
        doNothing().when(mockFeedController).sendPostAsync("post text");
        when(mComponent.feedController()).thenReturn(mockFeedController);
        getActivity();
        onView(withId(R.id.inputText))
                .perform(ViewActions.click())
                .perform(ViewActions.typeText("post text"));
        onView(withId(R.id.fab)).perform(ViewActions.click());
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockFeedController).sendPostAsync(captor.capture());
        assertThat(captor.getValue(), is("post text"));
        onView(withId(R.id.inputText)).check(matches(withText("")));
    }

    @Test
    public void refreshFeedWithNewItems() throws Throwable {
        FeedModel mockFeedModel = mock(FeedModel.class);
        when(mComponent.feedModel()).thenReturn(mockFeedModel);
        FeedItem feedItem1 = TestUtil.createDummyFeedItem();
        FeedItem feedItem2 = TestUtil.createDummyFeedItem();
        List<FeedItem> items = Collections.singletonList(feedItem1);
        List<FeedItem> items2 = Arrays.asList(feedItem1, feedItem2);
        Post post1 = feedItem1.getPost();
        final Post post2 = feedItem2.getPost();
        post1.setCreated(1L);
        post2.setCreated(2L);

        //noinspection unchecked
        when(mockFeedModel.loadFeed(0L, null)).thenReturn(items);
        when(mockFeedModel.loadFeed(post1.getCreated(), null)).thenReturn(items2);

        getActivity();
        onView(atAdapterPosition(getActivity().mBinding.list, 0))
                .check(matches(withChild(withText(post1.getText()))))
                .check(matches(withChild(withText(feedItem1.getUser().getName()))));
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().onEventMainThread(new FetchedFeedEvent(true, null, post2));
            }
        });
        onItemAt(0)
                .check(matches(withChild(withText(post2.getText()))))
                .check(matches(withChild(withText(feedItem2.getUser().getName()))));
        onItemAt(1)
                .check(matches(withChild(withText(post1.getText()))))
                .check(matches(withChild(withText(feedItem1.getUser().getName()))));
    }

    @Test
    public void refreshFeedWithOlderItems() throws Throwable {
        FeedModel mockFeedModel = mock(FeedModel.class);
        when(mComponent.feedModel()).thenReturn(mockFeedModel);
        FeedItem feedItem1 = TestUtil.createDummyFeedItem();
        FeedItem feedItem2 = TestUtil.createDummyFeedItem();
        List<FeedItem> items = Collections.singletonList(feedItem1);
        List<FeedItem> items2 = Arrays.asList(feedItem1, feedItem2);
        Post post1 = feedItem1.getPost();
        final Post post2 = feedItem2.getPost();
        post1.setCreated(10L);
        post2.setCreated(2L);

        //noinspection unchecked
        when(mockFeedModel.loadFeed(0L, null)).thenReturn(items);
        when(mockFeedModel.loadFeed(1L, null)).thenReturn(items2);

        getActivity();
        onView(atAdapterPosition(getActivity().mBinding.list, 0))
                .check(matches(withChild(withText(post1.getText()))))
                .check(matches(withChild(withText(feedItem1.getUser().getName()))));
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().onEventMainThread(new FetchedFeedEvent(true, null, post2));
            }
        });
        onItemAt(0)
                .check(matches(withChild(withText(post1.getText()))))
                .check(matches(withChild(withText(feedItem1.getUser().getName()))));
        onItemAt(1)
                .check(matches(withChild(withText(post2.getText()))))
                .check(matches(withChild(withText(feedItem2.getUser().getName()))));
    }


    private ViewInteraction onItemAt(int position) {
        return onView(atAdapterPosition(getActivity().mBinding.list, position));
    }

    private static Matcher<View> atAdapterPosition(final RecyclerView parent, final int position) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                return item.getParent() == parent && position == parent
                        .getChildAdapterPosition(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with " + position + " in RecyclerView");
            }
        };
    }
}
