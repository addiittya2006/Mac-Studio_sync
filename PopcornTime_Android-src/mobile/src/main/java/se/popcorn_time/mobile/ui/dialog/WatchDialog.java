package se.popcorn_time.mobile.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.model.PlayerInfo;
import se.popcorn_time.base.model.WatchInfo;
import se.popcorn_time.base.torrent.client.ClientConnectionListener;
import se.popcorn_time.base.torrent.client.WatchClient;
import se.popcorn_time.base.torrent.watch.WatchException;
import se.popcorn_time.base.torrent.watch.WatchListener;
import se.popcorn_time.base.torrent.watch.WatchProgress;
import se.popcorn_time.base.torrent.watch.WatchState;
import se.popcorn_time.base.utils.Logger;
import se.popcorn_time.mobile.ui.VLCPlayerActivity;
import se.popcorn_time.mobile.ui.base.PlayerBaseActivity;

public class WatchDialog extends DialogFragment implements WatchListener {

    private ImageView popcorn;
    private ViewGroup statusLayout;
    private ProgressBar statusProgress;
    private TextView statusInfo;
    private TextView statusPercent;

    private WatchInfo watchInfo;
    private WatchClient watchClient;
    private boolean prepared;
    private Animation popcornAnimation;

    private String loadedSubtitlesPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.FullscreenDialog);
        setCancelable(true);

        prepared = false;
        popcornAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.popcorn_prepare);

        watchClient = new WatchClient(getActivity());
        watchClient.setConnectionListener(new ClientConnectionListener() {
            @Override
            public void onClientConnected() {
                if (!prepared) {
                    watchClient.startWatch(watchInfo, WatchDialog.this);
                }
            }

            @Override
            public void onClientDisconnected() {

            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && KeyEvent.ACTION_UP == event.getAction()) {
                    close();
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_watch, container, false);

        popcorn = (ImageView) view.findViewById(R.id.prepare_popcorn);

        statusLayout = (ViewGroup) view.findViewById(R.id.prepare_status_layout);
        statusProgress = (ProgressBar) statusLayout.findViewById(R.id.prepare_status_progress);
        statusInfo = (TextView) statusLayout.findViewById(R.id.prepare_status_info);
        statusPercent = (TextView) statusLayout.findViewById(R.id.prepare_status_percent);

        ImageButton closeBtn = (ImageButton) view.findViewById(R.id.prepare_close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        watchClient.bind();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (prepared) {
            close();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        watchClient.removeWatchListener(WatchDialog.this);
        watchClient.unbind();
    }

    @Override
    public void onError(WatchException exception) {
        if (exception != null) {
            if (WatchState.LOAD_METADATA == exception.getState()) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.error_metadata, Toast.LENGTH_SHORT).show();
                }
            }
            Logger.error("State: " + exception.getState() + ", error: " + exception.getMessage());
        }
        stopAnim();
        dismiss();
    }

    @Override
    public void onMetadataLoad() {
        startAnim();
        loadedSubtitlesPath = null;
    }

    @Override
    public void onSubtitlesLoaded(String subPath) {
        loadedSubtitlesPath = subPath;
        Logger.debug("WatchDialog<onSubtitlesLoaded>: " + subPath);
    }

    @Override
    public void onDownloadStarted(String torrent) {
        Logger.debug("WatchDialog<onDownloadStarted>: " + torrent);
        stopAnim();
        prepared = false;
        if (statusLayout != null) {
            statusLayout.setVisibility(View.VISIBLE);
            updateStatus(0, 0, "0B/s");
        }
    }

    @Override
    public void onVideoPrepared(String filePath) {
        Logger.debug("WatchDialog<onPrepareWatchCompleted>: " + filePath);
        prepared = true;
        PlayerInfo playerInfo = new PlayerInfo();
        playerInfo.subtitles = watchInfo.subtitles;
        playerInfo.loadedSubtitlesPath = loadedSubtitlesPath;
        PlayerBaseActivity.start(getActivity(), new Intent(getActivity(), VLCPlayerActivity.class), Uri.parse("file://" + filePath), playerInfo);

//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.parse("file://" + filePath), "video/*");
//        try {
//            if (getActivity() != null) {
//                getActivity().startActivity(intent);
//            }
//        } catch (android.content.ActivityNotFoundException ex) {
//            Logger.error("WatchDialog<onPrepareWatchCompleted>: Error", ex);
//        }
    }

    @Override
    public void onUpdateProgress(WatchProgress progress) {
        updateStatus(progress.total, progress.value, progress.speed);
    }

    @Override
    public void onDownloadFinished() {
        Logger.debug("WatchDialog<onDownloadFinished>");
    }

    @Override
    public void onBufferingFinished() {

    }

    public void show(FragmentManager fm, WatchInfo watchInfo) {
        if (!isAdded()) {
            this.watchInfo = watchInfo;
            super.show(fm, "watch_dialog");
        }
    }

    public void close() {
        watchClient.stopWatch();
        dismiss();
    }

    private void startAnim() {
        if (popcorn != null) {
            popcorn.setVisibility(View.VISIBLE);
            if (popcornAnimation != null) {
                popcorn.startAnimation(popcornAnimation);
            }
        }
    }

    private void stopAnim() {
        if (popcorn != null) {
            popcorn.setVisibility(View.GONE);
            popcorn.clearAnimation();
        }
    }

    private void updateStatus(int max, int progress, String info) {
        statusProgress.setMax(max);
        statusProgress.setProgress(progress);
        statusInfo.setText(info);
        if (max > 0) {
            statusPercent.setText((100 * progress / max) + "%");
        } else {
            statusPercent.setText("0%");
        }
    }
}