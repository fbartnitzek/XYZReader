package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String CURRENT_POSITION = "current_position";
    private SwipeRefreshLayout mSwipeRefreshLayout; //seems to be unused ...
    private RecyclerView mRecyclerView;
    private static final String LOG_TAG = MainActivity.class.getName();
    private ArticleAdapter mArticleAdapter;
    private int mPosition = RecyclerView.NO_POSITION;


    private BroadcastReceiver mBroadcastReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.hasExtra(ArticleDetailActivity.CURSOR_POSITION)) {
                        mPosition = intent.getIntExtra(ArticleDetailActivity.CURSOR_POSITION, 0);
                        Log.v(LOG_TAG, "BroadcastReceiver.onReceive, hashCode=" + this.hashCode()
                                + ", mPosition=" + mPosition + ", " + "context = [" + context + "], intent = [" + intent + "]");
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate, " + "savedInstanceState = [" + savedInstanceState + "]");
        setContentView(R.layout.activity_article_list);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        mArticleAdapter = new ArticleAdapter(this, new ArticleAdapter.ArticleAdapterOnClickHandler() {
            @Override
            public void onClick(long id, ArticleAdapter.ArticleViewHolder vh) {
                Log.v(LOG_TAG, "onClick, " + "id = [" + id + "], vh = [" + vh
                        + "], transitionName: " + Utilities.TRANSITION_NAME_IMAGE_VIEW + id);
                Bundle bundle = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    bundle = ActivityOptions
                            .makeSceneTransitionAnimation(
                                    MainActivity.this,
                                    vh.thumbnailView,
                                    Utilities.TRANSITION_NAME_IMAGE_VIEW + id)
                            .toBundle();
                }
                startActivity(new Intent(Intent.ACTION_VIEW,
                        ItemsContract.Items.buildItemUri(id)), bundle);
            }
        });
        mRecyclerView.setAdapter(mArticleAdapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        //sglm.strategies... none: stops in middle of transition - not specific to none ..., move items is too dynamic...
//        sglm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
//        sglm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mRecyclerView.setLayoutManager(sglm);

        registerReceiver(mBroadcastReceiver,
                new IntentFilter(ArticleDetailActivity.BROADCAST_POSITION_CHANGE));

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        Log.v(LOG_TAG, "onStart, hashCode=" + this.hashCode() + ", " + "");
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
        if (mRecyclerView != null) {
            if (mPosition != RecyclerView.NO_POSITION){
                mRecyclerView.scrollToPosition(mPosition);
            }
        }
    }

    @Override
    protected void onStop() {
        Log.v(LOG_TAG, "onStop, hashCode=" + this.hashCode() + ", " + "");
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "onDestroy, hashCode=" + this.hashCode() + ", " + "");
        super.onDestroy();
        if (mBroadcastReceiver != null){
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG, "onSaveInstanceState, hashCode=" + this.hashCode() + ", " + "outState = [" + outState + "]");
        outState.putInt(CURRENT_POSITION, mPosition);
        super.onSaveInstanceState(outState);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "onCreateLoader, " + "i = [" + i + "], bundle = [" + bundle + "]");
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        //TODO: 2 times in log...?
        Log.v(LOG_TAG, "onLoadFinished, " + "cursorLoader = [" + cursorLoader + "], cursor = [" + cursor + "]");
        mArticleAdapter.swapCursor(cursor);
        // optional some preDraw-stuff...
//        Adapter adapter = new Adapter(cursor);
//        adapter.setHasStableIds(true);
//        mRecyclerView.setAdapter(adapter);
//        int columnCount = getResources().getInteger(R.integer.list_column_count);
//        StaggeredGridLayoutManager sglm =
//                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        mRecyclerView.setAdapter(null);
        Log.v(LOG_TAG, "onLoaderReset, " + "loader = [" + loader + "]");
        mArticleAdapter.swapCursor(null);
    }

    //TODO: 2 new links for reenter-bugs on changed-pager-pic
    // https://guides.codepath.com/android/Shared-Element-Activity-Transition
    // http://www.thedroidsonroids.com/blog/android/meaningful-motion-with-shared-element-transition-and-circular-reveal-animation/
    // TODO: try stuff from there...

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        // is not called ... :-(
        Log.v(LOG_TAG, "onActivityReenter, hashCode=" + this.hashCode() + ", " + "resultCode = [" + resultCode + "], data = [" + data + "]");
        super.onActivityReenter(resultCode, data);

        // postpone the shared element return transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportPostponeEnterTransition();
        }
    }

    /**
     * Schedules the shared element transition to be started immediately
     * after the shared element has been measured and laid out within the
     * activity's view hierarchy. Some common places where it might make
     * sense to call this method are:
     *
     * (1) Inside a Fragment's onCreateView() method (if the shared element
     *     lives inside a Fragment hosted by the called Activity).
     *
     * (2) Inside a Picasso Callback object (if you need to wait for Picasso to
     *     asynchronously load/scale a bitmap before the transition can begin).
     *
     * (3) Inside a LoaderCallback's onLoadFinished() method (if the shared
     *     element depends on data queried by a Loader).
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void scheduleStartPostponedTransition(final View sharedElement) {
        //http://www.androiddesignpatterns.com/2015/03/activity-postponed-shared-element-transitions-part3b.html
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        }
                        return true;
                    }
                });
    }
}
