package se.popcorn_time.mobile.ui.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.player.CastMobilePlayerActivity;
import com.player.MobilePlayerActivity;
import com.player.dialog.ListItemEntity;

import java.util.Locale;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.model.PlayerInfo;
import se.popcorn_time.base.storage.StorageUtil;
import se.popcorn_time.base.subtitles.Subtitles;
import se.popcorn_time.base.subtitles.SubtitlesFontColor;
import se.popcorn_time.base.subtitles.SubtitlesFontSize;
import se.popcorn_time.base.torrent.client.ClientConnectionListener;
import se.popcorn_time.base.torrent.client.PlayerClient;
import se.popcorn_time.base.torrent.watch.BaseWatchListener;
import se.popcorn_time.base.torrent.watch.WatchListener;
import se.popcorn_time.base.torrent.watch.WatchProgress;
import se.popcorn_time.base.torrent.watch.WatchState;
import se.popcorn_time.base.utils.InterfaceUtil;
import se.popcorn_time.base.utils.Logger;

public abstract class PlayerBaseActivity extends CastMobilePlayerActivity {

    private static final String EXTRA_PLAYER_INFO = "player-info";

    private final int DEFAULT_BUFFERING_PERCENT = 5;

    protected PlayerInfo playerInfo;
    private long seekPosition;
    private long downloadedProgress;

    private PlayerClient playerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Popcorn_Player_Classic);
        super.onCreate(savedInstanceState);

        setSubtitlesForeground(SubtitlesFontColor.getColor());
        setSubtitlesFontScale(SubtitlesFontSize.getSize());

        playerClient = new PlayerClient(PlayerBaseActivity.this);
        playerClient.setConnectionListener(new ClientConnectionListener() {
            @Override
            public void onClientConnected() {
                playerClient.addWatchListener(playerWatchListener);
                seekPosition = -1;
                downloadedProgress = 0;
            }

            @Override
            public void onClientDisconnected() {

            }
        });

        playerInfo = getIntent().getParcelableExtra(EXTRA_PLAYER_INFO);
        if (playerInfo != null && playerInfo.subtitles != null && playerInfo.subtitles.getLanguages().size() > Subtitles.START_INDEX) {
            for (int i = Subtitles.START_INDEX; i < playerInfo.subtitles.getLanguages().size(); i++) {
                ListItemEntity.addItemToList(subtitleItems, new SubtitleItem(playerInfo.subtitles.getUrls().get(i), playerInfo.subtitles.getLanguages().get(i)) {
                    @Override
                    public void onItemChosen() {
                        super.onItemChosen();
                        loadSubtitles(getValue());
                    }
                });
            }
            if (playerInfo.subtitles.getPosition() != Subtitles.WITHOUT_POSITION) {
                currentSubtitleItem = subtitleItems.get(playerInfo.subtitles.getPosition());
                if (TextUtils.isEmpty(playerInfo.loadedSubtitlesPath)) {
                    Logger.error("PlayerBaseActivity: For some reason subtitles was not loaded");
                    loadSubtitles(playerInfo.subtitles.getCurrentUrl());
                } else {
                    setSubtitlesTrack(playerInfo.loadedSubtitlesPath, null);
                }
            }
        } else {
            findBesideVideoSubtitles();
            if (subtitleItems.size() > 2) {
                subtitleItems.get(2).onItemChosen();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // set locale
        Locale.setDefault(InterfaceUtil.getAppLocale());
        Configuration config = getResources().getConfiguration();
        config.locale = InterfaceUtil.getAppLocale();
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void onStart() {
        super.onStart();
        playerClient.bind();
    }

    @Override
    protected void onStop() {
        super.onStop();
        playerClient.removeWatchListener(playerWatchListener);
        playerClient.unbind();
    }

    @Override
    protected void onStartSeeking() {
        super.onStartSeeking();
        seekPosition = -1;
    }

    @Override
    protected void onStopSeeking() {
        super.onStopSeeking();
        seekPosition = -1;
    }

    @Override
    protected void onSeek(long position) {
//        if (getPlayerControl() != null && playerClient.seek((float) position / getPlayerControl().getLength())) {
//            seekPosition = position;
//            hideInfo(false);
//            showBuffering();
//            updateBufferingProgress(DEFAULT_BUFFERING_PERCENT);
//            updateMediaTime(position, false);
//        } else {
//            hideBuffering();
//            super.onSeek(position);
//        }
//        updateMediaProgress(position);

        if (downloadedProgress == 0 || position < downloadedProgress) {
            super.onSeek(position);
        } else {
            hideInfo(false);
            onStopSeeking();
        }
    }

    @Override
    protected long getPlayerPosition() {
        if (seekPosition != -1) {
            return seekPosition;
        }
        return super.getPlayerPosition();
    }

    @Override
    protected String[] getDenyFolderNamesForCustomSubtitlesDialog() {
        return new String[]{
                StorageUtil.ROOT_FOLDER_NAME
        };
    }

    private WatchListener playerWatchListener = new BaseWatchListener() {
        @Override
        public void onUpdateProgress(WatchProgress progress) {
            if (progress == null) {
                return;
            }
            if (WatchState.SEQUENTIAL_DOWNLOAD == progress.state) {
                downloadedProgress = getPlayerControl() != null ? (long) ((double) progress.value / progress.total * getPlayerControl().getLength()) : 0;
                updateMediaProgress(downloadedProgress);
            } else if (WatchState.BUFFERING == progress.state) {
                int percent = progress.value * 100 / progress.total;
                if (percent < DEFAULT_BUFFERING_PERCENT) {
                    percent = DEFAULT_BUFFERING_PERCENT;
                } else if (percent > 100) {
                    percent = 100;
                }
                updateBufferingProgress(percent);
            }
        }

        @Override
        public void onDownloadFinished() {
            updateMediaProgress(getPlayerControl() != null ? getPlayerControl().getLength() : 0);
        }

        @Override
        public void onBufferingFinished() {
            if (seekPosition != -1) {
                hideBuffering();
                seek(seekPosition);
            }
        }
    };

    /*
    * Start
    * */

    public static void start(Context context, Intent intent, Uri uri, PlayerInfo playerInfo) {
        intent.putExtra(EXTRA_PLAYER_INFO, playerInfo);
        MobilePlayerActivity.start(context, intent, uri);
    }
}