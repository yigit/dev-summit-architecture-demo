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

package com.android.example.devsummit.archdemo.vo;

import com.android.example.devsummit.archdemo.model.DemoDatabase;
import com.android.example.devsummit.archdemo.util.DateUtil;
import com.android.example.devsummit.archdemo.util.Validation;
import com.android.example.devsummit.archdemo.util.ValidationFailedException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.apache.commons.lang3.StringUtils;

@Table(databaseName = DemoDatabase.NAME)
public class Post extends BaseModel implements Validation {
    @Column
    @PrimaryKey(autoincrement = false)
    @JsonProperty("id")
    long mId;
    @Column
    @JsonProperty("text")
    String mText;
    @Column
    @JsonIgnore
    long mCreated;
    @Column
    @JsonIgnore
    boolean mPending;
    @Column
    @JsonProperty("client_id")
    String mClientId;
    @Column
    @JsonProperty("user_id")
    long mUserId;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    @JsonIgnore
    public long getCreated() {
        return mCreated;
    }

    @JsonIgnore
    public void setCreated(long created) {
        this.mCreated = created;
    }

    public boolean isPending() {
        return mPending;
    }

    public void setPending(boolean pending) {
        this.mPending = pending;
    }

    public String getClientId() {
        return mClientId;
    }

    public void setClientId(String clientId) {
        this.mClientId = clientId;
    }

    public long getUserId() {
        return mUserId;
    }

    public void setUserId(long userId) {
        this.mUserId = userId;
    }

    @JsonProperty("created_at")
    public void setDate(String date) {
        mCreated = DateUtil.parseDate(date);
    }

    public String compositeUniqueKey() {
        return mUserId + "/" + mClientId;
    }

    public void validate() {
        if (mUserId < 1) {
            throw new ValidationFailedException("invalid user id");
        }
        if (mCreated < 1) {
            throw new ValidationFailedException("invalid created date");
        }
        if (StringUtils.isEmpty(mText)) {
            throw new ValidationFailedException("invalid text");
        }
        if (StringUtils.isEmpty(mClientId)) {
            throw new ValidationFailedException("invalid client id");
        }
    }
}
