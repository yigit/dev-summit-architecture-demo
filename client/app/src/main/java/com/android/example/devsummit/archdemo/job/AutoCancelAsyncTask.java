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

import com.android.example.devsummit.archdemo.util.LifecycleProvider;
import com.android.example.devsummit.archdemo.util.LifecycleListener;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public abstract class AutoCancelAsyncTask<Params, Result>
        extends AsyncTask<Params, Void, Result> implements LifecycleListener {

    private final WeakReference<LifecycleProvider> mLifecycleProviderRef;

    public AutoCancelAsyncTask(LifecycleProvider lifecycleProvider) {
        lifecycleProvider.addLifecycleListener(this);
        mLifecycleProviderRef = new WeakReference<>(lifecycleProvider);
    }

    @Override
    public void onProviderStopped() {
        cancel(false);
    }

    private void stopListening() {
        LifecycleProvider lifecycleProvider = mLifecycleProviderRef.get();
        if (lifecycleProvider != null) {
            lifecycleProvider.removeLifecycleListener(this);
        }
    }

    @Override
    protected final void onCancelled(Result result) {
        super.onCancelled(result);
        stopListening();
        onCancelled();
    }

    @SafeVarargs
    @Override
    protected final Result doInBackground(Params... params) {
        if (isCancelled()) {
            stopListening();
            return null;
        }
        try {
            return onDoInBackground(params);
        } catch (Throwable t) {
            stopListening();
            throw t;
        }
    }

    @Override
    protected final void onPostExecute(Result result) {
        stopListening();
        if (isCancelled()) {
            return;
        }
        onResult(result);
    }

    protected abstract void onResult(Result result);

    @SuppressWarnings("unchecked")
    protected abstract Result onDoInBackground(Params... params);

    @SuppressWarnings("unused")
    protected void onCancel() {
    }
}