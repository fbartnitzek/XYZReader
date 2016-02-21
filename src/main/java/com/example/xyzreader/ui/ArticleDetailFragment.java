package com.example.xyzreader.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
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
import com.example.xyzreader.data.Article;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link MainActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment{
    private static final String LOG_TAG = ArticleDetailFragment.class.getName();

    public static final String ARG_ARTICLE = "arg_article";
    private static final String STATE_ARTICLE = "state_article";

    private Article mArticle;
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

    public static ArticleDetailFragment newInstance(Article article) {
        Log.v(LOG_TAG, "newInstance" + ", " + "article = [" + article + "]");
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARG_ARTICLE, article);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);

        Log.v(LOG_TAG, "newInstance, " + "article= [" + article + "], hashcode=" + fragment.hashCode());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate, hashCode=" + this.hashCode() + ", " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ARTICLE)) {
            mArticle = getArguments().getParcelable(ARG_ARTICLE);
        } else if (savedInstanceState.containsKey(STATE_ARTICLE)) {
            mArticle = savedInstanceState.getParcelable(STATE_ARTICLE);
            bindViews();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mArticle != null) {
            outState.putParcelable(STATE_ARTICLE, mArticle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        Log.v(LOG_TAG, "onCreateView, hashcode=" + this.hashCode() + ", inflater = [" + inflater
                + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");

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
                        .setText(mArticle == null ? "Some sample text" :
                                mArticle.getTitle() + " by " + mArticle.getAuthor())
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();

        return mRootView;
    }

    private void bindViews() {
        Log.v(LOG_TAG, "bindViews, hashCode=" + this.hashCode() + ", " + "");
        if (mRootView == null || mArticle == null) {
            return;
        }
        Log.v(LOG_TAG, "bindViews, hashCode=" + this.hashCode() + ", transitionName: " +
                getString(R.string.transition_name_image_view) + "_" + mArticle.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPhotoView.setTransitionName(
                    getString(R.string.transition_name_image_view) + "_" + mArticle.getId());
        }

        mCollapsingToolbar.setTitle(mArticle.getTitle());
        mActionBar.setTitle(mArticle.getTitle());

        mArticleView.setText(
                getString(R.string.body_article,
                        Utilities.formatTimeSpan(
                                mArticle.getPublishedDate(),
                                System.currentTimeMillis()),
                        mArticle.getAuthor(),
                        Html.fromHtml(mArticle.getBody())));
        Log.v(LOG_TAG, "bindViews, hashCode=" + this.hashCode() + ", photo_url:" + mArticle.getPhotoUrl());

        Glide.with(getActivity())
                .load(mArticle.getPhotoUrl())
                        // test against white background picture...
//                .load("http://www.solidbackgrounds.com/images/1920x1080/1920x1080-white-solid-color-background.jpg")
                .centerCrop()
                .into(mPhotoView);

        ((ArticleDetailActivity) getActivity()).scheduleStartPostponedTransition(mPhotoView);
    }

}
