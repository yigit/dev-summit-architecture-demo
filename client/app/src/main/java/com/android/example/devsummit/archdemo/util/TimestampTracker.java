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

package com.android.example.devsummit.archdemo.util;

public class TimestampTracker {

    private static final long NaN = Long.MAX_VALUE;
    private long mCurrent = NaN;
    private long mNext = NaN;

    public void updateNext(long timestamp) {
        if (timestamp < mNext) {
            mNext = timestamp;
        }
    }

    public boolean hasTimestamp() {
        return mCurrent != NaN;
    }

    public void updateCurrent(long timestamp) {
        if (timestamp < mCurrent) {
            mCurrent = timestamp;
        }
    }

    public void swap() {
        mCurrent = mNext;
        mNext = NaN;
    }

    public void reset() {
        mCurrent = mNext = NaN;
    }

    public long getCurrent() {
        return mCurrent;
    }
}
