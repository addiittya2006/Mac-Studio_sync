package com.player;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.player.subtitles.FormatVTT;
import com.player.subtitles.SubtitlesUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import eu.sesma.castania.castserver.CastServerService;

public abstract class CastMobilePlayerActivity extends MobilePlayerActivity {

    final String APP_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;

    private TextTrackStyle mTrackStyle;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouterCallback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private RemoteMediaPlayer mRemoteMediaPlayer;

    private boolean mWaitingForReconnect = false;
    private boolean mApplicationStarted = false;
    private boolean mPlaying = false;

    private PlayerControl cachedPlayerControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTrackStyle = TextTrackStyle.fromSystemSettings(getBaseContext());
        mTrackStyle.setBackgroundColor(Color.parseColor("#00ffffff"));
        mTrackStyle.setEdgeType(TextTrackStyle.EDGE_TYPE_DROP_SHADOW);
        mTrackStyle.setEdgeColor(Color.parseColor("#bb000000"));

        mMediaRouter = MediaRouter.getInstance(getBaseContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))
                .build();
        mMediaRouterCallback = new MediaRouterCallback();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mMediaRouter.removeCallback(mMediaRouterCallback);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaRouter.removeCallback(mMediaRouterCallback);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        teardown(null, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_menu, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }

    @Override
    protected void onChosenWithoutSubtitles() {
        if (mApplicationStarted) {
            setSubtitlesEnabled(false);
        }
    }

    @Override
    protected void onChosenSubtitles() {
        if (mApplicationStarted) {
            final long position = mRemoteMediaPlayer.getApproximateStreamPosition();
            setPlayerControl(null);
            mRemoteMediaPlayer.stop(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                @Override
                public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                    if (result.getStatus().isSuccess()) {
                        castVideo(videoFile.getAbsolutePath(), position, videoFile.getName(), getVTTSubtitlesPath());
                    } else {
                        mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
                        teardown(null, false);
                    }
                }
            });
        }
    }

    @Override
    protected void setSubtitlesForeground(int color) {
        super.setSubtitlesForeground(color);
        if (mTrackStyle != null) {
            mTrackStyle.setForegroundColor(color);
        }
    }

    @Override
    protected void setSubtitlesFontScale(float scale) {
        super.setSubtitlesFontScale(scale);
        if (mTrackStyle != null) {
            mTrackStyle.setFontScale(scale);
        }
    }

    private PlayerControl castAction = new PlayerControl() {

        @Override
        public long getLength() {
            if (mRemoteMediaPlayer != null) {
                return mRemoteMediaPlayer.getStreamDuration();
            }
            return 0;
        }

        @Override
        public long getPosition() {
            if (mRemoteMediaPlayer != null) {
                return mRemoteMediaPlayer.getApproximateStreamPosition();
            }
            return 0;
        }

        @Override
        public boolean isPlaying() {
            return mPlaying;
        }

        @Override
        public void play() {
            if (mRemoteMediaPlayer != null && mApiClient != null) {
                mRemoteMediaPlayer.play(mApiClient);
            }
        }

        @Override
        public void pause() {
            if (mRemoteMediaPlayer != null && mApiClient != null) {
                mRemoteMediaPlayer.pause(mApiClient);
            }
        }

        @Override
        public void seek(long position) {
            if (mRemoteMediaPlayer != null && mApiClient != null) {
                mRemoteMediaPlayer.seek(mApiClient, position);
            }
        }

        @Override
        public void volumeUp() {
            if (/*mRemoteMediaPlayer != null && */mApiClient != null) {
                double currentVolume = Cast.CastApi.getVolume(mApiClient);
                try {
                    currentVolume = Math.min(currentVolume + 0.1, 1.0);
                    Cast.CastApi.setVolume(mApiClient, currentVolume);
                    showVolumeInfo((int) (currentVolume * 10), 10);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void volumeDown() {
            if (/*mRemoteMediaPlayer != null && */mApiClient != null) {
                double currentVolume = Cast.CastApi.getVolume(mApiClient);
                try {
                    currentVolume = Math.max(currentVolume - 0.1, 0.0);
                    Cast.CastApi.setVolume(mApiClient, currentVolume);
                    showVolumeInfo((int) (currentVolume * 10), 10);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /*
    * Media
    * */

    private void castVideo(String videoPath, long position, String title, String subtitlesPath) {
        int hostAddress = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getIpAddress();
        byte[] addressBytes = {
                (byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))
        };
        String ip;
        try {
            ip = InetAddress.getByAddress(addressBytes).getHostAddress();
        } catch (UnknownHostException e) {
            Log.e(TAG, "CastMobilePlayerActivity<castVideo>: Get device ip error", e);
            return;
        }

        int slash = videoPath.lastIndexOf('/');
        String rootDir = videoPath.substring(0, slash);
        String filename = videoPath.substring(slash + 1);

        startWebServer(ip, rootDir);

        final List<MediaTrack> tracks = new ArrayList<>();
        if (!TextUtils.isEmpty(subtitlesPath)) {
            String subtitlesName = subtitlesPath.substring(subtitlesPath.lastIndexOf('/') + 1);
            MediaTrack subtitleTrack = new MediaTrack.Builder(1, MediaTrack.TYPE_TEXT)
                    .setName(subtitlesName)
                    .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
                    .setContentId("http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + subtitlesName)
                    .build();
            tracks.add(subtitleTrack);
        }

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);

        MediaInfo mediaInfo = new MediaInfo.Builder("http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename)
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .setMediaTracks(tracks)
                .build();

        mRemoteMediaPlayer.load(mApiClient, mediaInfo, true, position).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
            @Override
            public void onResult(final RemoteMediaPlayer.MediaChannelResult result) {
                if (result.getStatus().isSuccess()) {
                    setPlayerControl(castAction);
                    mRemoteMediaPlayer.setTextTrackStyle(mApiClient, mTrackStyle);
                    if (tracks.size() > 0) {
                        setSubtitlesEnabled(true);
                    }
                } else {
                    mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
                    teardown(getString(R.string.cast_error_not_supported_media_type), false);
                }
            }
        });
    }

    @Nullable
    private String getVTTSubtitlesPath() {
        String subtitlesPath = SubtitlesUtils.generateSubtitlePath(videoFile.getAbsolutePath(), FormatVTT.EXTENSION);
        if (writeCurrentSubtitles(new FormatVTT(), subtitlesPath)) {
            return subtitlesPath;
        }
        return null;
    }

    private void setSubtitlesEnabled(boolean enable) {
        long[] tracks;
        if (enable) {
            tracks = new long[]{1};
        } else {
            tracks = new long[0];
        }
        try {
            mRemoteMediaPlayer.setActiveMediaTracks(mApiClient, tracks).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                @Override
                public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                    if (mediaChannelResult.getStatus().isSuccess()) {
                        Log.d(TAG, "CastMobilePlayerActivity<setSubtitlesEnabled>: Success");
                    } else {
                        Log.d(TAG, "CastMobilePlayerActivity<setSubtitlesEnabled>: Error");
                    }
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, "CastMobilePlayerActivity<setSubtitlesEnabled>: Error", ex);
        }
    }

    private void showCastErrorDialog(final PlayerControl playerControl, final String message) {
        DialogFragment castErrorDialog = new DialogFragment() {

            @Override
            public void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setCancelable(false);
            }

            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.cast_error_title);
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (playerControl != null) {
                            playerControl.play();
                        }
                    }
                });
                return builder.create();
            }
        };
        if (!isFinishing()) {
            castErrorDialog.show(getSupportFragmentManager(), "cast_error_dialog_" + castErrorDialog.hashCode());
        }
    }

    /*
    * Cast
    * */

    private void launchReceiver() {
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, new CastListener());
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(new ConnectionCallbacks())
                .addOnConnectionFailedListener(new ConnectionFailedListener())
                .build();
        mApiClient.connect();
    }

    private void connectMediaChannel() {
        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                if (mediaStatus != null) {
                    switch (mediaStatus.getPlayerState()) {
                        case MediaStatus.PLAYER_STATE_PLAYING:
                            mPlaying = true;
                            eventMediaPlaying();
//                            Log.d(TAG, "CastMobilePlayerActivity<onStatusUpdated>: PLAYING");
                            break;
                        case MediaStatus.PLAYER_STATE_PAUSED:
                            mPlaying = false;
                            eventMediaPaused();
//                            Log.d(TAG, "CastMobilePlayerActivity<onStatusUpdated>: PAUSED");
                            break;
                        case MediaStatus.PLAYER_STATE_IDLE:
//                            Log.d(TAG, "CastMobilePlayerActivity<onStatusUpdated>: IDLE");
                            break;
                        case MediaStatus.PLAYER_STATE_BUFFERING:
//                            Log.d(TAG, "CastMobilePlayerActivity<onStatusUpdated>: BUFFERING");
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        mRemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {

            }
        });
        reconnectMediaChannel();
        if (mApiClient != null) {
            mRemoteMediaPlayer.requestStatus(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                @Override
                public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.e(TAG, "CastMobilePlayerActivity<connectMediaChannel>: Failed to request status.");
                    }
                }
            });
        } else {
            //TODO: What do if GoogleApiClient is null?
        }
    }

    private void reconnectMediaChannel() {
        if (mApiClient == null || mRemoteMediaPlayer == null) {
            return;
        }
        try {
            Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
        } catch (IOException e) {
            Log.e(TAG, "CastMobilePlayerActivity<reconnectMediaChannel>: Error", e);
        }
    }

    private void disconnectMediaChannel() {
        if (mRemoteMediaPlayer != null) {
            if (mApiClient != null && Cast.CastApi != null) {
                try {
                    Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace());
                } catch (IOException e) {
                    Log.e(TAG, "CastMobilePlayerActivity<disconnectMediaChannel>: Error", e);
                }
            }
            mRemoteMediaPlayer = null;
        }
    }

    private void teardown(String errorMessage, boolean destroy) {
        boolean error = !TextUtils.isEmpty(errorMessage);
        if (error) {
            showCastErrorDialog(cachedPlayerControl, errorMessage);
        }
        if (cachedPlayerControl != null) {
            if (!destroy) {
                if (!error) {
                    int position = mRemoteMediaPlayer != null ? (int) mRemoteMediaPlayer.getApproximateStreamPosition() : -1;
                    if (position != -1) {
                        cachedPlayerControl.seek(position);
                    }
                    cachedPlayerControl.play();
                }
                setPlayerControl(cachedPlayerControl);
            }
            cachedPlayerControl = null;
        }
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected() || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient);
                        disconnectMediaChannel();
                    } catch (Exception ex) {
                        Log.e(TAG, "CastMobilePlayerActivity<teardown>: Error", ex);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        stopCastServer();
    }

    /*
    * Classes
    * */

    private class MediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            cachedPlayerControl = getPlayerControl();
            setPlayerControl(null);
            if (cachedPlayerControl != null) {
                cachedPlayerControl.pause();
            }
            mSelectedDevice = CastDevice.getFromBundle(route.getExtras());
            launchReceiver();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            teardown(null, false);
            mSelectedDevice = null;
        }
    }

    private class CastListener extends Cast.Listener {

        @Override
        public void onApplicationDisconnected(int statusCode) {
            teardown(null, false);
        }

        @Override
        public void onApplicationStatusChanged() {

        }

        @Override
        public void onVolumeChanged() {

        }
    }

    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle bundle) {
            if (mWaitingForReconnect) {
                mWaitingForReconnect = false;
                reconnectMediaChannel();
            } else {
                try {
                    Cast.CastApi.launchApplication(mApiClient, APP_ID, false).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
                        @Override
                        public void onResult(Cast.ApplicationConnectionResult result) {
                            Status status = result.getStatus();
                            if (status.isSuccess()) {
                                connectMediaChannel();
                                mApplicationStarted = true;
                                long position = cachedPlayerControl != null ? cachedPlayerControl.getPosition() : 0;
                                castVideo(videoFile.getAbsolutePath(), position, videoFile.getName(), getVTTSubtitlesPath());
                            } else {
                                teardown(null, false);
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "CastMobilePlayerActivity<onConnected>: Error", e);
                }
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            mWaitingForReconnect = true;
        }
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            teardown(null, false);
        }
    }

    /*
    * Web Server
    * */

    private void startWebServer(final String ip, final String rootDir) {
        Intent castServerService = new Intent(CastMobilePlayerActivity.this, CastServerService.class);
        castServerService.putExtra(CastServerService.IP_ADDRESS, ip);
        castServerService.putExtra(CastServerService.ROOT_DIR, rootDir);
        startService(castServerService);
    }

    private void stopCastServer() {
        stopService(new Intent(CastMobilePlayerActivity.this, CastServerService.class));
    }
}