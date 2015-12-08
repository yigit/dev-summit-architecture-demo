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

import com.path.android.jobqueue.log.CustomLogger;

import android.util.Log;

public class L {

    private static final String TAG = "[DEMO]";

    public static void d(String msg, Object... args) {
        Log.d(TAG, String.format(msg, args));
    }

    public static void e(Throwable t, String msg, Object... args) {
        Log.e(TAG, String.format(msg, args), t);
    }

    public static CustomLogger getJobLogger() {
        return instance;
    }

    private static final CustomLogger instance = new CustomLogger() {
        @Override
        public boolean isDebugEnabled() {
            return true;
        }

        @Override
        public void d(String text, Object... args) {
            L.d(text, args);
        }

        @Override
        public void e(Throwable t, String text, Object... args) {
            L.e(t, text, args);
        }

        @Override
        public void e(String text, Object... args) {
            L.d(text, args);
        }
    };
}
