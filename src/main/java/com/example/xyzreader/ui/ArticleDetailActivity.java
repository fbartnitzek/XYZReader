package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;

import com.example.xyzreader.R;
import com.example.xyzreader.data.Article;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ViewPager.OnPageChangeListener {

    private static final String LOG_TAG = ArticleDetailActivity.class.getName();
    private Cursor mCursor;
    private long mStartId;

    private long mSelectedItemId;   //TODO: should be stored...

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate, hashCode=" + this.hashCode() + ", " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//        }
        setContentView(R.layout.activity_article_detail);

        getLoaderManager().initLoader(0, null, this);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        // what might that be ...
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.addOnPageChangeListener(this);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // nothing
    }

    @Override
    public void onPageSelected(int position) {
        Log.v(LOG_TAG, "onPageSelected, hashCode=" + this.hashCode() + ", " + "position = [" + position + "]");
        if (mCursor != null) {
            mCursor.moveToPosition(position);
        }
        mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // nothing
    }

    @Override
    protected void onDestroy() {
        mPager.removeOnPageChangeListener(this);
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "onCreateLoader, hashCode=" + this.hashCode() + ", " + "i = [" + i + "], bundle = [" + bundle + "]");
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(LOG_TAG, "onLoadFinished, hashCode=" + this.hashCode() + ", " + "cursorLoader = [" + cursorLoader + "], cursor = [" + cursor + "]");
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID from state
        if (mStartId > 0) {
            mCursor.moveToFirst();
            while (!mCursor.isAfterLast() && mCursor.getLong(ArticleLoader.Query._ID) != mStartId) {
                mCursor.moveToNext();
            }

            Log.v(LOG_TAG, "onLoadFinished, " + "position: " + mCursor.getPosition());
            mPager.setCurrentItem(mCursor.getPosition(), false);
            mStartId = 0;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(LOG_TAG, "onLoaderReset, hashCode=" + this.hashCode() + ", " + "cursorLoader = [" + cursorLoader + "]");
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        private final String LOG_TAG = MyPagerAdapter.class.getName();
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            Log.v(LOG_TAG, "MyPagerAdapter, hashCode=" + this.hashCode() + ", " + "fm = [" + fm + "]");
        }

        @Override
        public Fragment getItem(int position) {
            Log.v(LOG_TAG, "getItem, hashCode=" + this.hashCode() + ", " + "position = [" + position + "]");
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(
                    new Article(
                            mCursor.getString(ArticleLoader.Query.AUTHOR),
                            mCursor.getString(ArticleLoader.Query.BODY),
                            mCursor.getString(ArticleLoader.Query.PHOTO_URL),
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            mCursor.getString(ArticleLoader.Query.TITLE)
                    ));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
