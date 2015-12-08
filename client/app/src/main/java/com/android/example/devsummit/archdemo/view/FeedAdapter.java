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

package com.android.example.devsummit.archdemo.view;

import com.android.example.devsummit.archdemo.R;
import com.android.example.devsummit.archdemo.databinding.FeedItemBinding;
import com.android.example.devsummit.archdemo.util.L;
import com.android.example.devsummit.archdemo.vo.FeedItem;
import com.android.example.devsummit.archdemo.vo.Post;
import com.android.example.devsummit.archdemo.vo.User;

import android.content.ClipData;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedItemViewHolder> {

    final LayoutInflater mLayoutInflater;

    final SortedList<FeedItem> mList;

    final Map<String, FeedItem> mUniqueMapping = new HashMap<>();

    private Callback mCallback;

    public FeedAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mList = new SortedList<>(FeedItem.class,
                new SortedListAdapterCallback<FeedItem>(this) {
                    @Override
                    public int compare(FeedItem o1, FeedItem o2) {
                        Post p2 = o2.getPost();
                        Post p1 = o1.getPost();
                        if (p1.isPending() != p2.isPending()) {
                            return p1.isPending() ? -1 : 1;
                        }
                        return (int) (p2.getCreated() - p1.getCreated());
                    }

                    @SuppressWarnings("SimplifiableIfStatement")
                    @Override
                    public boolean areContentsTheSame(FeedItem oldItem,
                            FeedItem newItem) {
                        Post oldPost = oldItem.getPost();
                        Post newPost = newItem.getPost();
                        if (oldPost.getId() != newPost.getId()) {
                            return false;
                        }
                        if (!oldPost.getText().equals(newPost.getText())) {
                            return false;
                        }
                        if (!oldItem.getUser().getName().equals(newItem.getUser().getName())) {
                            return false;
                        }
                        return oldItem.getPost().isPending() == newItem.getPost().isPending();
                    }

                    @Override
                    public boolean areItemsTheSame(FeedItem item1, FeedItem item2) {
                        return item1.getPost().getId() == item2.getPost().getId();
                    }
                });
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public FeedItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final FeedItemBinding binding = DataBindingUtil.inflate(mLayoutInflater,
                R.layout.feed_item, parent, false);
        FeedItemViewHolder holder = new FeedItemViewHolder(binding);
        holder.binding.userName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCallback == null) {
                    return;
                }
                FeedItem model = binding.getModel();
                mCallback.onUserClick(model.getUser());
            }
        });
        // we don't need grid layout error messages
        holder.binding.grid.setPrinter(null);
        return holder;
    }

    @Override
    public void onBindViewHolder(FeedAdapter.FeedItemViewHolder holder, int position) {
        holder.binding.setModel(mList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void insert(FeedItem item) {
        String key = createKeyFor(item.getPost());
        FeedItem existing = mUniqueMapping.put(key, item);
        if (existing == null) {
            mList.add(item);
        } else {
            int pos = mList.indexOf(existing);
            mList.updateItemAt(pos, item);
        }
    }

    public void updatePost(Post post) {
        String key = createKeyFor(post);
        FeedItem existing = mUniqueMapping.get(key);
        if (existing == null) {
            L.d("update post is received but it does not exist, ignoring... %s", key);
            return;
        }
        int pos = mList.indexOf(existing);
        FeedItem newItem = new FeedItem(post, existing.getUser());
        mUniqueMapping.put(key, newItem);
        mList.updateItemAt(pos, newItem);
    }

    public void insertAll(List<FeedItem> items) {
        for (FeedItem item : items) {
            insert(item);
        }
    }

    public void swapList(List<FeedItem> items) {
        Set<String> newListKeys = new HashSet<>();
        for (FeedItem item : items) {
            newListKeys.add(createKeyFor(item.getPost()));
        }
        for (int i = mList.size() - 1; i >= 0; i--) {
            FeedItem item = mList.get(i);
            String key = createKeyFor(item.getPost());
            if (!newListKeys.contains(key)) {
                mUniqueMapping.remove(key);
                mList.removeItemAt(i);
            }
        }
        insertAll(items);
    }

    public long getReferenceTimestamp() {
        int size = mList.size();
        if (size == 0) {
            return 0;
        }
        return mList.get(0).getPost().getCreated();
    }

    public void removePost(Post post) {
        FeedItem model = mUniqueMapping.remove(createKeyFor(post));
        if (model != null) {
            mList.remove(model);
        }
    }

    public void clear() {
        mList.clear();
        mUniqueMapping.clear();
    }

    public static class FeedItemViewHolder extends RecyclerView.ViewHolder {
        public final FeedItemBinding binding;
        public FeedItemViewHolder(FeedItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static String createKeyFor(Post post) {
        return post.compositeUniqueKey();
    }

    public interface Callback {
        void onUserClick(User user);
    }
}
