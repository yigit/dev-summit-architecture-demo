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

package com.android.example.devsummit.archdemo.api;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface ApiService {

    @GET("/feed.json")
    Call<FeedResponse> feed(@Query("since") long since);

    @GET("/user_feed/{userId}.json")
    Call<FeedResponse> userFeed(@Path("userId") long userId, @Query("since") long since);

    @POST("new_post.json")
    Call<NewPostResponse> sendPost(@Query("text") String text, @Query("client_id") String clientId,
            // ultra secure API sending user id :p
            @Query("user_id") long userId);
}
