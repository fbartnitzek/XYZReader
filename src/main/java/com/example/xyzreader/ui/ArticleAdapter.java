package com.example.xyzreader.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * Copyright 2016.  Frank Bartnitzek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private static final String LOG_TAG = ArticleAdapter.class.getName();
    private final Context mContext;
    private final ArticleAdapterOnClickHandler mClickHandler;

    private Cursor mCursor;

    public ArticleAdapter(Context context, ArticleAdapterOnClickHandler ch) {
        super();
        Log.v(LOG_TAG, "ArticleAdapter, " + "context = [" + context + "], ch = [" + ch + "]");
        mContext = context;
        mClickHandler = ch;
        //optional: empty view...
    }

    // callback for MainActivity
    public interface ArticleAdapterOnClickHandler {
        void onClick(long aLong, ArticleViewHolder vh);
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public DynamicHeightImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            thumbnailView = (DynamicHeightImageView) itemView.findViewById(R.id.thumbnail);
            titleView = (TextView) itemView.findViewById(R.id.article_title);
            subtitleView = (TextView) itemView.findViewById(R.id.article_subtitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.v(LOG_TAG, "onClick, " + "v = [" + v + "]");
            mCursor.moveToPosition(getAdapterPosition());
            mClickHandler.onClick(mCursor.getLong(ArticleLoader.Query._ID), this);
        }
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            // View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_article, parent, false);
            view.setFocusable(true);    //some a11y-stuff...
            return new ArticleViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder, " + "holder = [" + holder + "], position = [" + position + "]");
        mCursor.moveToPosition(position);

        // title
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

        // subtitle: publishTime by Author
        holder.subtitleView.setText(
                mContext.getString(R.string.subtitle_text,
                        Utilities.formatTimeSpan(
                                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                                System.currentTimeMillis()),
                        mCursor.getString(ArticleLoader.Query.AUTHOR)
                )
        );

        // image
        Glide.with(mContext)
                .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                .centerCrop()
                .into(holder.thumbnailView);
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        //optional: hide empty view if itemCount>0
    }

    public Cursor getCursor() {
        return mCursor;
    }

    //optional...?
    //selectView
    //getSelectedItemPosition


}
