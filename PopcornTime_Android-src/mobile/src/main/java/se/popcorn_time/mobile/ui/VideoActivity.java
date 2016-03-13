package se.popcorn_time.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.loader.HttpProviderLoader;
import se.popcorn_time.base.model.video.category.Anime;
import se.popcorn_time.base.model.video.category.Cinema;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;
import se.popcorn_time.mobile.ui.base.PopcornLoadActivity;
import se.popcorn_time.mobile.ui.base.VideoTypeFragment;

public class VideoActivity extends PopcornLoadActivity implements LoaderManager.LoaderCallbacks<VideoInfo> {

    public static final String VIDEO_INFO_KEY = "video-info";

    private final int INFO_LOADER_ID = 1001;

    private VideoTypeFragment videoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Popcorn_Classic);
        super.onCreate(savedInstanceState);
        setPopcornContentViewId(R.id.content);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        restartExtraInfoLoader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.video_settings:
                SettingsActivity.start(VideoActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(INFO_LOADER_ID);
    }

    @Override
    public void retryLoad() {
        restartExtraInfoLoader();
    }

    @Override
    public Loader<VideoInfo> onCreateLoader(int id, Bundle args) {
        if (INFO_LOADER_ID == id) {
            ArrayList<VideoInfoProvider> providers = args.getParcelableArrayList("providers");
            VideoInfo info = args.getParcelable("info");
            showLoading();
            return new HttpProviderLoader<>(VideoActivity.this, providers, info);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<VideoInfo> loader, VideoInfo data) {
        if (INFO_LOADER_ID == loader.getId()) {
            if (data != null) {
                onInfoLoaded(data);
            } else {
                showError();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<VideoInfo> loader) {

    }

    private void restartExtraInfoLoader() {
        VideoInfo videoInfo = getIntent().getExtras().getParcelable(VIDEO_INFO_KEY);
        if (videoInfo != null) {
            ArrayList<VideoInfoProvider> providers = videoInfo.getVideoInfoProviders();
            if (providers == null) {
                onInfoLoaded(videoInfo);
            } else {
                Bundle data = new Bundle();
                data.putParcelableArrayList("providers", providers);
                data.putParcelable("info", videoInfo);
                getSupportLoaderManager().restartLoader(INFO_LOADER_ID, data, VideoActivity.this);
            }
        }
    }

    private void onInfoLoaded(VideoInfo data) {
        switch (data.getVideoType()) {
            case Cinema.TYPE_MOVIES:
            case Anime.TYPE_MOVIES:
                videoFragment = new VideoMovieFragment();
                break;
            case Cinema.TYPE_TV_SHOWS:
            case Anime.TYPE_TV_SHOWS:
                videoFragment = new VideoTVShowFragment();
                break;
            default:
                break;
        }
        if (videoFragment != null) {
            Bundle args = new Bundle();
            args.putParcelable(VIDEO_INFO_KEY, data);
            videoFragment.setArguments(args);
            replaceFragment(videoFragment);
        }
    }

    public static void start(Context context, VideoInfo videoInfo) {
        Bundle extras = new Bundle();
        extras.putParcelable(VideoActivity.VIDEO_INFO_KEY, videoInfo);
        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtras(extras);
        context.startActivity(intent);
    }
}