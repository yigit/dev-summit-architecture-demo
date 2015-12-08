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

import org.apache.commons.lang3.StringUtils;

import com.android.example.devsummit.archdemo.model.DemoDatabase;
import com.android.example.devsummit.archdemo.util.Validation;
import com.android.example.devsummit.archdemo.util.ValidationFailedException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = DemoDatabase.NAME)
public class User extends BaseModel implements Validation {
    @Column
    @PrimaryKey(autoincrement = false)
    @JsonProperty("id")
    long mId;

    @Column
    @JsonProperty("name")
    String mName;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    @Override
    public void validate() {
        if (mId < 1) {
            throw new ValidationFailedException("invalid user id");
        }
        if (StringUtils.isEmpty(mName)) {
            throw new ValidationFailedException("invalid mName");
        }
    }
}
