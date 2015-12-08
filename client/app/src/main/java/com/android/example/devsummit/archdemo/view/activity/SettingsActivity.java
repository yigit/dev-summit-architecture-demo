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
import com.android.example.devsummit.archdemo.databinding.SettingsBinding;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public class SettingsActivity extends BaseActivity {
    @Inject
    DemoConfig mDemoConfig;

    private SettingsBinding mBinding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.settings);
        mBinding.setConfig(mDemoConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            long newUid = Long.parseLong(mBinding.userId.getText().toString());
            mDemoConfig.setUserId(newUid);
        } catch (Throwable ignored){}
        try {
            int newPostRetryCount = Integer.parseInt(mBinding.newPostRetryCount.getText().toString());
            mDemoConfig.setNewPostRetryCount(newPostRetryCount);
        } catch (Throwable ignored){}
        mDemoConfig.setApiUrl(mBinding.apiUrl.getText().toString());
    }

    public static Intent intentFor(Context source) {
        return new Intent(source, SettingsActivity.class);
    }
}
