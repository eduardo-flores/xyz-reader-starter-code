package com.example.xyzreader.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
public class ArticleListActivity extends AppCompatActivity implements ArticleListAdapter.IArticleItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private static int index = -1;
    private static int top = -1;
    private long mBackPressed;
    private GridLayoutManager gridLayoutManager;
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        setupSwipe();
        setupRecyclerView();

        if (savedInstanceState == null) {
            refresh();
        }

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private void setupRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);

        int columnCount = DividerItemDecoration.getHorizontalSpanCount(this);
        gridLayoutManager = new GridLayoutManager(this, columnCount);
        mRecyclerView.setLayoutManager(gridLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    private void setupSwipe() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.theme_primary_light,
                R.color.theme_primary,
                R.color.theme_primary_dark);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);

        getSupportLoaderManager().destroyLoader(0);
    }

    private boolean mIsRefreshing = false;

    @Override
    public void onBackPressed() {
        int timeDelay = 1500;

        if (mBackPressed + timeDelay > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), getString(R.string.exit_message),
                    Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        ArticleListAdapter articleListAdapter = new ArticleListAdapter(cursor, this);
        articleListAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(articleListAdapter);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        index = gridLayoutManager.findFirstCompletelyVisibleItemPosition();

        View v = mRecyclerView.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - mRecyclerView.getPaddingTop());
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(0, null, this);

        if (index != -1) {
            gridLayoutManager.scrollToPositionWithOffset(index, top);
        }
    }

    @Override
    public void onClick(long articleId, String title, String author, String imageUrl, ImageView thumbnail) {
        Intent detailIntent = new Intent(ArticleListActivity.this, ArticleDetailActivity.class);
        detailIntent.setAction(Intent.ACTION_VIEW);
        detailIntent.setData(ItemsContract.Items.buildItemUri(articleId));
        detailIntent.putExtra("title", title);
        detailIntent.putExtra("author", author);
        detailIntent.putExtra("image", imageUrl);

        if (!getResources().getBoolean(R.bool.isLand)) {
            detailIntent.putExtra("image-transition", ViewCompat.getTransitionName(thumbnail));

            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this,
                            thumbnail,
                            ViewCompat.getTransitionName(thumbnail));

            startActivity(detailIntent, activityOptionsCompat.toBundle());
        } else {
            startActivity(detailIntent);
        }
    }
}