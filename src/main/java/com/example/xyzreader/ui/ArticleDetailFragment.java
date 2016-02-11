package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link MainActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ArticleDetailFragment.class.getName();

    public static final String ARG_ITEM_ID = "item_id";

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;

    private ImageView mPhotoView;

    private CollapsingToolbarLayout mCollapsingToolbar;

    private ActionBar mActionBar;
    private TextView mArticleView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        setHasOptionsMenu(true);
    }

    //???
    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        Log.v(LOG_TAG, "onCreateView, " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");

        mPhotoView = (ImageView) mRootView.findViewById(R.id.toolbar_photo);

        // actionbar
        mCollapsingToolbar = (CollapsingToolbarLayout) mRootView.findViewById(
                R.id.collapsing_toolbar);

        // modifiable actionbar - from activity, just to be sure...
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        activity.setSupportActionBar((Toolbar) mRootView.findViewById(R.id.toolbar_text));
        mActionBar = activity.getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mArticleView = (TextView) mRootView.findViewById(R.id.article_body);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        return mRootView;
    }

    //TODO: react on change with pagerView everywhere...

    private void bindViews() {
        Log.v(LOG_TAG, "bindViews, mCursor:" + mCursor);
        if (mRootView == null || mCursor == null) {
            return;
        }

        mCollapsingToolbar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
        mActionBar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));


        mArticleView.setText(
                getString(R.string.body_article,
                        Utilities.formatTimeSpan(
                                mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                                System.currentTimeMillis()),
                        mCursor.getString(ArticleLoader.Query.AUTHOR),
                        Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY))));
        Log.v(LOG_TAG, "bindViews, photo_url:" + mCursor.getString(ArticleLoader.Query.PHOTO_URL));
        Glide.with(getActivity())
                .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                        // test against white background picture...
//                .load("http://www.solidbackgrounds.com/images/1920x1080/1920x1080-white-solid-color-background.jpg")
                .centerCrop()
                .into(mPhotoView);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(LOG_TAG, "onLoadFinished, " + "cursorLoader = [" + cursorLoader + "], cursor = [" + cursor + "]");
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(LOG_TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        Log.v(LOG_TAG, "onLoaderReset, " + "cursorLoader = [" + cursorLoader + "]");
        bindViews();
    }

}
