package se.popcorn_time.mobile.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.model.DownloadInfo;
import se.popcorn_time.base.model.video.info.Episode;
import se.popcorn_time.base.model.video.info.Season;
import se.popcorn_time.base.model.video.info.Torrent;
import se.popcorn_time.base.model.video.info.TvShowsInfo;
import se.popcorn_time.mobile.ui.adapter.EpisodeAdapter;
import se.popcorn_time.mobile.ui.adapter.SeasonAdapter;
import se.popcorn_time.mobile.ui.base.VideoTypeFragment;

public class VideoTVShowFragment extends VideoTypeFragment {

    private TvShowsInfo tvShowsInfo;

    private ListView seasonsView;
    private ListView episodesView;
    private TextView episodeTitle;
    private TextView episodeDescription;

    private SeasonAdapter mSeasonAdapter;
    private EpisodeAdapter mEpisodeAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvShowsInfo = (TvShowsInfo) videoInfo;
        mSeasonAdapter = new SeasonAdapter(getActivity(), tvShowsInfo.getSeasons());
        mEpisodeAdapter = new EpisodeAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_video_tvshow, container, false);
        populateView(view);
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onChangeScreenOrientation(R.layout.fragment_video_tvshow);
    }

    @Override
    public void updateLocaleText() {
        super.updateLocaleText();
        if (tvShowsInfo.getSeasons().size() > 0) {
            mSeasonAdapter.notifyDataSetInvalidated();
            mEpisodeAdapter.notifyDataSetInvalidated();
            updateTorrents();
        }
    }

    @Override
    protected void populateView(View view) {
        super.populateView(view);
        LinearLayout infoView = (LinearLayout) view.findViewById(R.id.video_tvshow_info_view);
        if (poster != null && infoView != null) {
            poster.addOnLayoutChangeListener(new OnPosterChangeListener(infoView));
        }
        if (tvShowsInfo.getSeasons().size() > 0) {
            view.findViewById(R.id.video_content_view).setVisibility(View.VISIBLE);

            seasonsView = (ListView) view.findViewById(R.id.video_seasons);
            seasonsView.setAdapter(mSeasonAdapter);
            seasonsView.setOnItemClickListener(seasonListener);

            episodesView = (ListView) view.findViewById(R.id.video_episodes);
            episodesView.setAdapter(mEpisodeAdapter);
            episodesView.setOnItemClickListener(episodeListener);

            episodeTitle = (TextView) view.findViewById(R.id.video_episode_title);
            episodeDescription = (TextView) view.findViewById(R.id.video_episode_description);

            seasonsView.post(new Runnable() {
                @Override
                public void run() {
                    int seasonPosition = tvShowsInfo.getSeasonPosition();
                    seasonsView.performItemClick(mSeasonAdapter.getView(seasonPosition, null, null), seasonPosition, mSeasonAdapter.getItemId(seasonPosition));
                }
            });
        } else {
            view.findViewById(R.id.video_content_view).setVisibility(View.INVISIBLE);
        }
        updateLocaleText();
    }

    @Override
    protected void sendCustomSubtitle(File file) {

    }

    @Override
    protected DownloadInfo buildDownloadInfo(@NonNull Torrent torrent) {
        DownloadInfo info = super.buildDownloadInfo(torrent);
        Season season = tvShowsInfo.getCurrentSeason();
        if (season != null) {
            info.season = season.getNumber();
        }
        Episode episode = tvShowsInfo.getCurrentEpisode();
        if (episode != null) {
            info.episode = episode.getNumber();
        }
        return info;
    }

    private OnItemClickListener seasonListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            tvShowsInfo.setSeasonPosition(position);
            mSeasonAdapter.setSelectedItem(position);
            Season season = tvShowsInfo.getCurrentSeason();
            if (season != null) {
                mEpisodeAdapter.replaceData(season.episodes);
            } else {
                mEpisodeAdapter.replaceData(null);
            }
            if (!changeOrientation) {
                tvShowsInfo.setEpisodePosition(0);
            }
            episodesView.post(new Runnable() {
                @Override
                public void run() {
                    int episodePosition = tvShowsInfo.getEpisodePosition();
                    episodesView.performItemClick(mEpisodeAdapter.getView(episodePosition, null, null), episodePosition, mEpisodeAdapter.getItemId(episodePosition));
                    episodesView.setSelection(episodePosition);
                }
            });
        }
    };

    private OnItemClickListener episodeListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            tvShowsInfo.setEpisodePosition(position);
            mEpisodeAdapter.setSelectedItem(position);
            Episode episode = tvShowsInfo.getCurrentEpisode();
            if (episode != null) {
                episodeTitle.setText(Html.fromHtml("<b>" + episode.title + "</b>"));
                if (TextUtils.isEmpty(episode.description)) {
                    episodeDescription.setVisibility(View.GONE);
                } else {
                    episodeDescription.setVisibility(View.VISIBLE);
                    episodeDescription.setText(Html.fromHtml(episode.description));
                }
            }
            if (changeOrientation) {
                changeOrientation = false;
            } else {
                videoInfo.setTorrentPosition(0);
                restartSubtitlesLoader();
            }
            updateTorrents();
        }
    };

    private class OnPosterChangeListener implements View.OnLayoutChangeListener, Runnable {

        private ViewGroup infoView;
        private Handler handler = new Handler();
        private int width;

        public OnPosterChangeListener(ViewGroup infoView) {
            this.infoView = infoView;
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            int _width = v.getMeasuredWidth();
            if (width != _width) {
                width = _width;
                handler.removeCallbacks(OnPosterChangeListener.this);
                handler.postDelayed(OnPosterChangeListener.this, 150);
            }
        }

        @Override
        public void run() {
            ViewGroup.LayoutParams params = infoView.getLayoutParams();
            params.width = width;
            infoView.setLayoutParams(params);
        }
    }
}