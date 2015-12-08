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

import com.android.example.devsummit.archdemo.R;
import com.android.example.devsummit.archdemo.config.DemoConfig;
import com.android.example.devsummit.archdemo.controller.FeedController;
import com.android.example.devsummit.archdemo.databinding.ActivityFeedBinding;
import com.android.example.devsummit.archdemo.event.SubscriberPriority;
import com.android.example.devsummit.archdemo.event.feed.FetchedFeedEvent;
import com.android.example.devsummit.archdemo.event.post.DeletePostEvent;
import com.android.example.devsummit.archdemo.event.post.NewPostEvent;
import com.android.example.devsummit.archdemo.event.post.UpdatedPostEvent;
import com.android.example.devsummit.archdemo.job.AutoCancelAsyncTask;
import com.android.example.devsummit.archdemo.model.FeedModel;
import com.android.example.devsummit.archdemo.util.L;
import com.android.example.devsummit.archdemo.util.TimestampTracker;
import com.android.example.devsummit.archdemo.view.FeedAdapter;
import com.android.example.devsummit.archdemo.vo.FeedItem;
import com.android.example.devsummit.archdemo.vo.Post;
import com.android.example.devsummit.archdemo.vo.User;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class FeedActivity extends BaseActivity {
    private static final String EXTRA_USER_ID = "user_id";
    private static final String EXTRA_USER_NAME = "user_name";
    private static final String EXTRA_POST_TEXT = "post_text";

    @Inject
    EventBus mEventBus;

    @Inject
    FeedModel mFeedModel;

    @Inject
    DemoConfig mDemoConfig;

    @Inject
    FeedController mFeedController;

    LinearLayoutManager mLinearLayoutManager;

    private FeedAdapter mFeedAdapter;

    ActivityFeedBinding mBinding;

    // Lock to avoid creating multiple refresh jobs
    private boolean mPendingRefresh = false;

    // Tracks event timestamps which will be used when querying disk for new items
    private final TimestampTracker mTimestampTracker = new TimestampTracker();

    // Whose feed is this. COMMON_FEED_USER_ID is common feed
    private Long mUserId;

    // Set when Activity starts. Since activity does not listen for events after being stopped, we
    // need to do a full sync on return. Event cycle can be moved between onCreate/onDestroy to
    // avoid this but that will require additional complexity of checking when to update the views.
    private boolean mRefreshFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        long userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        mUserId = userId == -1 ? null : userId;

        if (mUserId == null) {
            setTitle(R.string.feed_title);
        } else {
            String name = getIntent().getStringExtra(EXTRA_USER_NAME);
            setTitle(getString(R.string.user_feed_title, name));
        }
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_feed);
        boolean showInput = mUserId == null || mUserId == mDemoConfig.getUserId();
        mBinding.inputText.setVisibility(showInput ? View.VISIBLE : View.GONE);
        String predefinedInput = getIntent().getStringExtra(EXTRA_POST_TEXT);
        if (StringUtils.isNotBlank(predefinedInput)) {
            mBinding.inputText.setText(predefinedInput);
        }
        initRecyclerView();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPost();
            }
        });
        mBinding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mFeedController.fetchFeedAsync(true, mUserId);
            }
        });
    }

    public void setUserId(Long userId) {
        mUserId = userId;
    }

    private void sendPost() {
        String text = mBinding.inputText.getText().toString().trim();
        if (StringUtils.isEmpty(text)) {
            return;
        }
        mFeedController.sendPostAsync(text);
        mBinding.inputText.setText(null);
    }

    private void initRecyclerView() {
        mFeedAdapter = new FeedAdapter(this);
        mFeedAdapter.setCallback(new FeedAdapter.Callback() {
            @Override
            public void onUserClick(User user) {
                startActivity(intentFor(FeedActivity.this, user.getId(), user.getName()));
            }
        });
        mBinding.list.setAdapter(mFeedAdapter);
        mBinding.list.setHasFixedSize(true);
        mLinearLayoutManager = (LinearLayoutManager) mBinding.list.getLayoutManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRefreshFull = true;
        mEventBus.register(this, SubscriberPriority.HIGH);
        refresh(null);
        mFeedController.fetchFeedAsync(true, mUserId);
    }

    private void refresh(@Nullable Post referencePost) {
        if (mPendingRefresh) {
            if (referencePost != null) {
                mTimestampTracker.updateNext(referencePost.getCreated() - 1);
            }
            return;
        }
        if (referencePost != null) {
            mTimestampTracker.updateCurrent(referencePost.getCreated() - 1);
        }
        final long reference;
        final boolean swapList = mRefreshFull;
        mRefreshFull = false;
        if (swapList) {
            reference = 0L;
        } else if(mTimestampTracker.hasTimestamp()) {
            reference = Math.min(mTimestampTracker.getCurrent(),
                    mFeedAdapter.getReferenceTimestamp());
        } else {
            reference = mFeedAdapter.getReferenceTimestamp();
        }

        L.d("refreshing with reference time %s", reference);
        new AutoCancelAsyncTask<Void, List<FeedItem>>(this){
            @Override
            protected void onResult(List<FeedItem> feedItems) {
                L.d("feed model returned with %s items", feedItems.size());
                if (swapList) {
                    mFeedAdapter.swapList(feedItems);
                } else {
                    mFeedAdapter.insertAll(feedItems);
                }

                if (mLinearLayoutManager.findFirstVisibleItemPosition() == 0) {
                    mLinearLayoutManager.scrollToPosition(0);
                }
                mTimestampTracker.swap();
                mPendingRefresh = false;
                if (mTimestampTracker.hasTimestamp()) {
                    mBinding.getRoot().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refresh(null);
                        }
                    }, 1);
                }
            }

            @Override
            protected List<FeedItem> onDoInBackground(Void... params) {
                L.d("time to query feed model");
                return mFeedModel.loadFeed(reference, mUserId);
            }
        }.execute();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UpdatedPostEvent event) {
        L.d("received post update %s", event.getPost().getText());
        mFeedAdapter.updatePost(event.getPost());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NewPostEvent event) {
        if (mUserId == null || event.getPost().getUserId() == mUserId) {
            refresh(event.getPost());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(FetchedFeedEvent event) {
        //noinspection NumberEquality
        if (mUserId == null ? event.getUserId() == null : mUserId == event.getUserId()) {
            if (event.isSuccess()) {
                refresh(event.getOldest());
            } else {
                Snackbar.make(mBinding.coordinatorLayout,
                        R.string.cannot_refresh_feed, Snackbar.LENGTH_SHORT).show();
            }
            mBinding.swipeContainer.setRefreshing(false);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DeletePostEvent event) {
        if (event.isSyncFailure()) {
            event.markAsNotifiedUser();
            Snackbar.make(mBinding.coordinatorLayout,
                    getString(R.string.cannot_sync_post, event.getText()),
                    Snackbar.LENGTH_LONG).show();
            if (StringUtils.isEmpty(mBinding.inputText.getText().toString().trim())) {
                // recover text in case user wants to put it in again
                mBinding.inputText.setText(event.getText());
            }
        }
        if (event.getPost() != null) {
            mFeedAdapter.removePost(event.getPost());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mEventBus.unregister(this);
        mBinding.swipeContainer.setRefreshing(false);
        mPendingRefresh = false;
        mTimestampTracker.reset();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(SettingsActivity.intentFor(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Intent intentForSendPost(Context context, String postContent) {
        Intent intent = new Intent(context, FeedActivity.class);
        intent.putExtra(EXTRA_POST_TEXT, postContent);
        return intent;
    }
    public static Intent intentFor(Context context, long userId, String userName) {
        Intent intent = new Intent(context, FeedActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_USER_NAME, userName);
        return intent;
    }
}
