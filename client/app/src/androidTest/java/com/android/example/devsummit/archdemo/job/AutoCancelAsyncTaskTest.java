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

package com.android.example.devsummit.archdemo.job;

import com.android.example.devsummit.archdemo.util.LifecycleListener;
import com.android.example.devsummit.archdemo.util.LifecycleProvider;

import org.junit.Test;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AutoCancelAsyncTaskTest {
    @Test
    public void unregisterAfterCompletion() {
        Executor mockExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
        LifecycleProvider provider = mock(LifecycleProvider.class);
        doNothing().when(provider).addLifecycleListener(any(LifecycleListener.class));
        AutoCancelAsyncTask<Void, Void> task = new AutoCancelAsyncTask<Void, Void>(provider) {
            @Override
            protected void onResult(Void aVoid) {

            }

            @Override
            protected Void onDoInBackground(Void... params) {
                return null;
            }
        };
        task.executeOnExecutor(mockExecutor);
        verify(provider).removeLifecycleListener(any(LifecycleListener.class));
    }
    @Test
    public void unregisterOnCancel() {
        LifecycleProvider provider = mock(LifecycleProvider.class);
        doNothing().when(provider).addLifecycleListener(any(LifecycleListener.class));
        AutoCancelAsyncTask<Void, Void> task = new AutoCancelAsyncTask<Void, Void>(provider) {
            @Override
            protected void onResult(Void aVoid) {

            }

            @Override
            protected Void onDoInBackground(Void... params) {
                return null;
            }
        };
        task.cancel(false);
        verify(provider).removeLifecycleListener(any(LifecycleListener.class));
    }
}
