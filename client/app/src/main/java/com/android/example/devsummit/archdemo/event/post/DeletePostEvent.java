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

package com.android.example.devsummit.archdemo.event.post;

import com.android.example.devsummit.archdemo.vo.Post;

import android.support.annotation.Nullable;

public class DeletePostEvent {

    @Nullable
    private final Post mPost;
    private final boolean mSyncFailure;
    private final String mText;
    private boolean mNotifiedUser;

    public DeletePostEvent(boolean syncFailure, String text, @Nullable Post post) {
        mPost = post;
        mSyncFailure = syncFailure;
        mText = text;
    }

    public void markAsNotifiedUser() {
        mNotifiedUser = true;
    }

    public boolean didNotifyUser() {
        return mNotifiedUser;
    }

    @Nullable
    public Post getPost() {
        return mPost;
    }

    public boolean isSyncFailure() {
        return mSyncFailure;
    }

    public String getText() {
        return mText;
    }
}
