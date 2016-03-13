package se.popcorn_time.mobile.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.GridView;

import java.util.ArrayList;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.loader.HttpProviderLoader;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.providers.video.list.VideoListProvider;
import se.popcorn_time.mobile.ui.adapter.VideoAdapter;
import se.popcorn_time.mobile.ui.locale.LocaleFragment;

public class GridVideoFragment extends LocaleFragment implements LoaderManager.LoaderCallbacks<ArrayList<VideoInfo>> {

    private static final String VIDEO_PROVIDER_KEY = "video-provider";
    private static final String VIDEO_LIST_KEY = "video-list";

    final int ITEMS_COUNT_FOR_LOADING = 75;
    final int PAGE_ERROR_DELAY = 2500;
    final int MAX_PAGE_COUNT = 6;
    final int ROW_COUNT_FOR_NEXT_PAGE = 4;

    private final int PAGE_LOADER_ID = 1001;

    private VideoListProvider provider;
    private GridView videoGrid;
    private VideoAdapter videoAdapter;
    private boolean canLoadPage;

    private FrameLayout contentFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        provider = getArguments().getParcelable(VIDEO_PROVIDER_KEY);
        ArrayList<VideoInfo> info = getArguments().getParcelableArrayList(VIDEO_LIST_KEY);
        videoAdapter = new VideoAdapter(getActivity(), info);
        canLoadPage = info.size() >= ITEMS_COUNT_FOR_LOADING;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentFrame = new FrameLayout(getActivity().getBaseContext());
        return populateContentView(inflater, contentFrame);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // change orientation
        if (contentFrame != null) {
            contentFrame.removeAllViewsInLayout();
            populateContentView(LayoutInflater.from(getActivity().getBaseContext()), contentFrame);
        }
    }

    @Override
    public void onDestroy() {
        getLoaderManager().destroyLoader(PAGE_LOADER_ID);
        super.onDestroy();
    }

    @Override
    public Loader<ArrayList<VideoInfo>> onCreateLoader(int id, Bundle args) {
        return new HttpProviderLoader<>(getActivity(), provider);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<VideoInfo>> loader, ArrayList<VideoInfo> data) {
        if (data != null) {
            if (data.size() > 0) {
                canLoadPage = data.size() >= ITEMS_COUNT_FOR_LOADING;
                videoAdapter.addData(data);
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    provider.decrementPage();
                    canLoadPage = true;
                }
            }, PAGE_ERROR_DELAY);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<VideoInfo>> loader) {

    }

    private View populateContentView(LayoutInflater inflater, ViewGroup content) {
        videoGrid = (GridView) inflater.inflate(R.layout.fragment_grid_video, content, false);
        videoGrid.setAdapter(videoAdapter);
        videoGrid.setOnScrollListener(contentScrollListener);
        content.addView(videoGrid);
        return content;
    }

    private void restartVideosPageLoader() {
        provider.incrementPage();
        if (provider.getPage() <= MAX_PAGE_COUNT) {
            getActivity().getSupportLoaderManager().restartLoader(PAGE_LOADER_ID, null, GridVideoFragment.this);
        }
    }

    private OnScrollListener contentScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (canLoadPage && totalItemCount != 0) {
                int dec = totalItemCount - firstVisibleItem;
                if (dec <= videoGrid.getNumColumns() * ROW_COUNT_FOR_NEXT_PAGE) {
                    canLoadPage = false;
                    restartVideosPageLoader();
                }
            }
        }
    };

    public static Bundle createArguments(VideoListProvider provider, ArrayList<VideoInfo> data) {
        Bundle args = new Bundle();
        args.putParcelable(VIDEO_PROVIDER_KEY, provider);
        args.putParcelableArrayList(VIDEO_LIST_KEY, data);
        return args;
    }
}