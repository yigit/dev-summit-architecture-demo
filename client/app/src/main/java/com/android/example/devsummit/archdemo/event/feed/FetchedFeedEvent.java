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

package com.android.example.devsummit.archdemo.event.feed;

import com.android.example.devsummit.archdemo.vo.Post;

import android.support.annotation.Nullable;

public class FetchedFeedEvent {

    private final boolean mSuccess;
    @Nullable
    private final Long mUserId;
    private final Post mOldest;

    public FetchedFeedEvent(boolean success, @Nullable Long userId, @Nullable Post oldest) {
        mSuccess = success;
        mUserId = userId;
        mOldest = oldest;
    }

    public boolean isSuccess() {
        return mSuccess;
    }

    @Nullable
    public Long getUserId() {
        return mUserId;
    }

    @Nullable
    public Post getOldest() {
        return mOldest;
    }
}
