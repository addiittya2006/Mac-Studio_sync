package se.popcorn_time.mobile.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.database.tables.Downloads;
import se.popcorn_time.base.model.DownloadInfo;
import se.popcorn_time.base.model.WatchInfo;
import se.popcorn_time.base.model.video.category.Anime;
import se.popcorn_time.base.model.video.category.Cinema;
import se.popcorn_time.base.torrent.TorrentState;
import se.popcorn_time.base.torrent.client.DownloadsClient;
import se.popcorn_time.base.storage.StorageUtil;
import se.popcorn_time.mobile.ui.dialog.DownloadsMoreDialog;
import se.popcorn_time.mobile.ui.dialog.OptionDialog;
import se.popcorn_time.mobile.ui.dialog.WatchDialog;

public class DownloadsAdapter extends CursorAdapter {

    private FragmentActivity activity;
    private DownloadsClient downloadsClient;
    private DownloadsMoreDialog moreDialog;
    private OptionDialog removeDialog;
    private WatchDialog watchDialog;

    private String sizeText;
    private String seasonText;
    private String episodeText;
    private String finishedText;
    private String pausedText;
    private String errorText;
    private String checkingDataText;
    private String watchBtnText;

    public DownloadsAdapter(FragmentActivity activity, DownloadsClient downloadsClient) {
        super(activity, null, false);
        this.activity = activity;
        this.downloadsClient = downloadsClient;
    }

    public void updateLocaleText() {
        sizeText = activity.getString(R.string.size);
        seasonText = activity.getString(R.string.season);
        episodeText = activity.getString(R.string.episode);
        finishedText = activity.getString(R.string.finished);
        pausedText = activity.getString(R.string.paused);
        errorText = activity.getString(R.string.error_metadata);
        checkingDataText = activity.getString(R.string.checking_data);
        watchBtnText = activity.getString(R.string.watch_it_now);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        DownloadView downloadView = (DownloadView) view;
        DownloadInfo info = new DownloadInfo();
        try {
            info.populate(cursor);
            downloadView.populate(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new DownloadView(context);
    }

    private void showMoreDialog(DownloadInfo info) {
        if (moreDialog == null) {
            moreDialog = new DownloadsMoreDialog();
        }
        if (!moreDialog.isAdded()) {
            moreDialog.setInfo(info);
            moreDialog.setListener(moreListener);
            moreDialog.show(activity.getSupportFragmentManager(), "download_more_dialog");
        }
    }

    private void showRemoveDialog(DownloadInfo info) {
        if (removeDialog == null) {
            removeDialog = new OptionDialog();
        }
        if (!removeDialog.isAdded()) {
            removeDialog.setListener(new RemoveListener(info));
            removeDialog.setArguments(OptionDialog.createArguments(activity.getString(R.string.remove), activity.getString(R.string.downloads_remove_msg)));
            removeDialog.show(activity.getSupportFragmentManager(), "downloads_remove_dialog");
        }
    }

    private DownloadsMoreDialog.DownloadsMoreListener moreListener = new DownloadsMoreDialog.DownloadsMoreListener() {
        @Override
        public void onDownloadsPause(DownloadInfo info) {
            downloadsClient.downloadsPause(info);
        }

        @Override
        public void onDownloadsResume(DownloadInfo info) {
            downloadsClient.downloadsResume(info);
        }

        @Override
        public void onDownloadsRemove(DownloadInfo info) {
            showRemoveDialog(info);
        }

        @Override
        public void onDownloadsRetry(DownloadInfo info) {
            downloadsClient.downloadsRetry(info);
        }
    };

    private class RemoveListener extends OptionDialog.SimpleOptionListener {

        private DownloadInfo info;

        public RemoveListener(DownloadInfo info) {
            this.info = info;
        }

        @Override
        public boolean positiveShow() {
            return true;
        }

        @Override
        public String positiveButtonText() {
            return activity.getString(android.R.string.ok);
        }

        @Override
        public void positiveAction() {
            downloadsClient.downloadsRemove(info);
        }

        @Override
        public boolean neutralShow() {
            return true;
        }

        @Override
        public String neutralButtonText() {
            return activity.getString(android.R.string.cancel);
        }
    }

    private class DownloadView extends LinearLayout {

        private final int STATE_WHAT = 1;
        private final int HANDLER_DELAY = 1000;

        private RoundedImageView poster;
        private TextView title;
        private TextView summary;
        private Button more;
        private ProgressBar progress;
        private TextView status;
        private TextView progressPercentage;
        private Button watchNow;

        private DownloadInfo info;
        private int fileSizeMB;
        private int progressSizeMB;
        private String torrentFile;
        private String dots;

        public DownloadView(Context context) {
            super(context);
            init();
        }

        public DownloadView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public DownloadView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            init();
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            stateHandler.sendEmptyMessage(STATE_WHAT);
        }

        @Override
        protected void onDetachedFromWindow() {
            stateHandler.removeMessages(STATE_WHAT);
            super.onDetachedFromWindow();
        }

        public void populate(DownloadInfo info) {
            this.info = info;
            torrentFile = downloadsClient.getAvailableTorrentFile(info);
            dots = "";
            fileSizeMB = (int) (info.size / StorageUtil.SIZE_MB);

            Picasso.with(getContext()).load(info.posterUrl).placeholder(R.drawable.poster).into(poster);
            title.setText(info.title);
            more.setOnClickListener(moreListener);
            String summaryTxt;
            switch (info.type) {
                case Cinema.TYPE_MOVIES:
                case Anime.TYPE_MOVIES:
                    summaryTxt = sizeText + " " + StorageUtil.getSizeText(info.size);
                    break;
                case Cinema.TYPE_TV_SHOWS:
                case Anime.TYPE_TV_SHOWS:
                    summaryTxt = seasonText + " " + info.season + ", " + episodeText + " " + info.episode + "\n"
                            + sizeText + " " + StorageUtil.getSizeText(info.size);
                    break;
                default:
                    summaryTxt = "Unknown type: " + info.type;
                    break;
            }
            summary.setText(summaryTxt);
            watchNow.setText(watchBtnText);
            watchNow.setOnClickListener(watchListener);
        }

        private void init() {
            inflate(getContext(), R.layout.item_list_download, DownloadView.this);
            poster = (RoundedImageView) findViewById(R.id.download_poster);
            title = (TextView) findViewById(R.id.download_title);
            summary = (TextView) findViewById(R.id.download_summary);
            more = (Button) findViewById(R.id.download_more);
            progress = (ProgressBar) findViewById(R.id.download_progress);
            status = (TextView) findViewById(R.id.download_status);
            progressPercentage = (TextView) findViewById(R.id.download_progress_percentage);
            watchNow = (Button) findViewById(R.id.download_watchnow);
        }

        private void checkState() {
            if (TextUtils.isEmpty(torrentFile)) {
                torrentFile = downloadsClient.getAvailableTorrentFile(info);
            }
            updateProgress();
            if (TorrentState.DOWNLOADING == info.state) {
                if (TextUtils.isEmpty(torrentFile)) {
                    progress(-1);
                } else {
                    int state = downloadsClient.getTorrentState(torrentFile);
                    if (TorrentState.FINISHED == state || TorrentState.SEEDING == state) {
                        if (fileSizeMB > 0 && fileSizeMB <= progressSizeMB) {
                            info.state = TorrentState.FINISHED;
                            Downloads.update(activity, info);
                            progress(TorrentState.FINISHED);
                        } else {
                            progress(state);
                        }
                    } else {
                        progress(state);
                    }
                }
            } else if (TorrentState.FINISHED == info.state) {
                progress(TorrentState.FINISHED);
            } else if (TorrentState.PAUSED == info.state) {
                if (!TextUtils.isEmpty(torrentFile)) {
                    int state = downloadsClient.getTorrentState(torrentFile);
                    if (TorrentState.FINISHED == state || TorrentState.SEEDING == state) {
                        if (fileSizeMB > 0 && fileSizeMB <= progressSizeMB) {
                            info.state = TorrentState.FINISHED;
                            Downloads.update(activity, info);
                            progress(TorrentState.FINISHED);
                            return;
                        }
                    }
                }
                progress(TorrentState.PAUSED);
            } else if (TorrentState.ERROR == info.state) {
                progress(TorrentState.ERROR);
            } else {
                progress(-1);
            }
        }

        private void progress(int state) {
            if (TorrentState.DOWNLOADING == state) {
                status.setText(downloadsClient.getTorrentSpeed(torrentFile));
            } else if (TorrentState.FINISHED == state) {
                status.setText(finishedText);
            } else if (TorrentState.PAUSED == state) {
                status.setText(pausedText);
            } else if (TorrentState.ERROR == state) {
                status.setText(errorText);
            } else {
                status.setText(checkingDataText + dots);
                if (dots.length() >= 3) {
                    dots = "";
                } else {
                    dots += ".";
                }
            }
        }

        private void updateProgress() {
            if (TextUtils.isEmpty(torrentFile)) {
                progressSizeMB = 0;
            } else {
                progressSizeMB = downloadsClient.getDownloadSizeMb(torrentFile);
            }
            progress.setMax(fileSizeMB);
            progress.setProgress(progressSizeMB);
            int percentage = (int) (((double) progressSizeMB / (double) fileSizeMB) * 100);
            progressPercentage.setText(percentage + "%");
        }

        private OnClickListener moreListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                showMoreDialog(info);
            }
        };

        private OnClickListener watchListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(torrentFile)) {
                    if (watchDialog == null) {
                        watchDialog = new WatchDialog();
                    }
                    watchDialog.show(activity.getSupportFragmentManager(), new WatchInfo(info, null));
                }
            }
        };

        private Handler stateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                checkState();
                stateHandler.sendEmptyMessageDelayed(STATE_WHAT, HANDLER_DELAY);
                return true;
            }
        });
    }
}
