package com.example.xyzreader.ui;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;

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
                        + "], transitionName: " + getString(R.string.transition_name_image_view) + "_" + id);
                Bundle bundle = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    bundle = ActivityOptions
                            .makeSceneTransitionAnimation(
                                    MainActivity.this,
                                    vh.thumbnailView,
                                    getString(R.string.transition_name_image_view) + "_" + id)
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
        //TODO: sglm.strategies...?
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
//            Log.v(LOG_TAG, "onStart, hashCode=" + this.hashCode() + ", with recyclerview" + "");
            if (mPosition != RecyclerView.NO_POSITION){
                mRecyclerView.scrollToPosition(mPosition);
            }
//        } else {
//            Log.v(LOG_TAG, "onStart, hashCode=" + this.hashCode() + ", without recyclerview..." + "");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null){
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
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

}
