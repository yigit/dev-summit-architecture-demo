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

package com.android.example.devsummit.archdemo.model;

import com.android.example.devsummit.archdemo.App;
import com.android.example.devsummit.archdemo.util.ValidationUtil;
import com.android.example.devsummit.archdemo.vo.User;
import com.android.example.devsummit.archdemo.vo.User$Table;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserModel extends BaseModel {

    public UserModel(App app, SQLiteDatabase database) {
        super(app, database);
    }

    public void save(User user) {
        user.validate();
        user.save();
    }

    public void saveAll(final List<User> users) {
        ValidationUtil.pruneInvalid(users);
        if (users.isEmpty()) {
            return;
        }
        TransactionManager.transact(mSQLiteDatabase, new Runnable() {
            @Override
            public void run() {
                for (User user : users) {
                    user.save();
                }
            }
        });
    }

    public User load(long id) {
        return new Select().from(User.class)
                .where(Condition.column(User$Table.MID).eq(id))
                .querySingle();
    }

    public Map<Long, User> loadUsersAsMap(List<Long> userIds) {
        final HashMap<Long, User> result = new HashMap<>();
        if (userIds.isEmpty()) {
            return result;
        }
        long first = userIds.get(0);
        List<Long> rest = userIds.subList(1, userIds.size());
        FlowCursorList<User> userFlowCursorList = new Select().from(User.class).where(
                Condition.column(User$Table.MID).in(first, rest.toArray())
        ).queryCursorList();
        try {
            final int size = userFlowCursorList.getCount();
            for (int i = 0; i < size; i++) {
                User user = userFlowCursorList.getItem(i);
                result.put(user.getId(), user);
            }
        } finally {
            userFlowCursorList.close();
        }

        return result;
    }
}
