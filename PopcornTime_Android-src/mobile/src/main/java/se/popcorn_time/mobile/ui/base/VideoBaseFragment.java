package se.popcorn_time.mobile.ui.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.player.dialog.FileChooserDialog;
import com.player.dialog.ListItemEntity;
import com.player.subtitles.SubtitlesRenderer;
import com.player.subtitles.SubtitlesUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.database.tables.Downloads;
import se.popcorn_time.base.loader.HttpProviderLoader;
import se.popcorn_time.base.model.DownloadInfo;
import se.popcorn_time.base.model.WatchInfo;
import se.popcorn_time.base.model.video.info.Torrent;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.prefs.PopcornPrefs;
import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.providers.subtitles.SubtitlesProvider;
import se.popcorn_time.base.storage.StorageUtil;
import se.popcorn_time.base.subtitles.Subtitles;
import se.popcorn_time.base.torrent.TorrentState;
import se.popcorn_time.base.torrent.client.DownloadsClient;
import se.popcorn_time.base.utils.Logger;
import se.popcorn_time.base.utils.NetworkUtil;
import se.popcorn_time.base.vpn.VpnManager;
import se.popcorn_time.mobile.ui.DownloadsActivity;
import se.popcorn_time.mobile.ui.VideoActivity;
import se.popcorn_time.mobile.ui.dialog.OptionDialog;
import se.popcorn_time.mobile.ui.dialog.VpnDialog;
import se.popcorn_time.mobile.ui.dialog.WatchDialog;
import se.popcorn_time.mobile.ui.locale.LocaleFragment;
import se.popcorn_time.mobile.ui.widget.ItemSelectButton;

public abstract class VideoBaseFragment extends LocaleFragment {

    private final int HANDLER_DOWNLOAD = 1;
    private final int HANDLER_WATCH_DOWNLOAD = 2;
    private final int HANDLER_WATCH = 3;

    private final int DOWNLOADS_REQUEST_CODE = 101;
    private final int STARS_COUNT = 5;
    private final float MAX_RATING = 10;
    final float RATING_COEF = STARS_COUNT / MAX_RATING;

    protected VideoInfo videoInfo;
    protected Subtitles subtitles;
    protected boolean changeOrientation = false;
    private boolean isDownloads;
    private FileChooserDialog customSubtitleDialog;
    private WatchDialog watchDialog;
    private OptionDialog optionDialog = new OptionDialog();

    // view
    protected ImageView poster;
    private ItemSelectButton subtitlesButton;
    private ItemSelectButton torrentsButton;
    private Button downloadOpenBtn;
    private Button watchItNow;

    private DownloadsClient downloadsClient;

    protected abstract void sendCustomSubtitle(File file);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoInfo = getArguments().getParcelable(VideoActivity.VIDEO_INFO_KEY);
        downloadsClient = new DownloadsClient(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        downloadsClient.bind();
    }

    @Override
    public void onStop() {
        super.onStop();
        downloadsClient.unbind();
    }

    @Override
    public void updateLocaleText() {
        super.updateLocaleText();
        subtitlesButton.updateText();
        updateTorrents();
        watchItNow.setText(R.string.watch_it_now);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DOWNLOADS_REQUEST_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                checkIsDownloads();
            }
        }
    }

    protected void onChangeScreenOrientation(int resourceId) {
        changeOrientation = true;
        if (isResumed() && customSubtitleDialog != null && customSubtitleDialog.isAdded()) {
            customSubtitleDialog.dismiss();
        }
        ViewGroup container = (ViewGroup) getView();
        if (container != null) {
            container.removeAllViewsInLayout();
            View view = LayoutInflater.from(getActivity()).inflate(resourceId, container, false);
            container.addView(view);
            mLocaleHelper.updateLocale();
            populateView(view);
        }
    }

    protected void populateView(View view) {
        poster = (ImageView) view.findViewById(R.id.video_poster);
        if (poster != null) {
            Picasso.with(getActivity()).load(videoInfo.posterBigUrl).placeholder(R.drawable.poster).resize(390, 585).into(poster);
        }
        TextView title = (TextView) view.findViewById(R.id.video_title);
        if (title != null) {
            title.setText(Html.fromHtml("<b>" + videoInfo.title + "</b>"));
        }
        TextView description = (TextView) view.findViewById(R.id.video_description);
        if (description != null) {
            description.setText(Html.fromHtml(videoInfo.description));
        }
        RatingBar rating = (RatingBar) view.findViewById(R.id.video_rating);
        if (rating != null) {
            rating.setRating(videoInfo.rating * RATING_COEF);
        }

        downloadOpenBtn = (Button) view.findViewById(R.id.video_download_open);
        watchItNow = (Button) view.findViewById(R.id.video_watchitnow);
        watchItNow.setOnClickListener(watchItNowListener);

        subtitlesButton = (ItemSelectButton) view.findViewById(R.id.video_subtitles);
        subtitlesButton.setFragmentManager(getFragmentManager());
        subtitlesButton.setPrompt(R.string.subtitles);
        updateSubtitles();

        torrentsButton = (ItemSelectButton) view.findViewById(R.id.video_torrents);
        torrentsButton.setFragmentManager(getFragmentManager());
        torrentsButton.setPrompt(R.string.torrents);
    }

    protected void updateTorrents() {
        int torrentCount = videoInfo.getTorrents() != null ? videoInfo.getTorrents().size() : 0;
        if (torrentCount > 0) {
            List<ListItemEntity> items = new ArrayList<>();
            for (Torrent torrent : videoInfo.getTorrents()) {
                ListItemEntity.addItemToList(items, new ListItemEntity<Torrent>(torrent) {
                    @Override
                    public String getName() {
                        return getValue().quality
                                + ", " + getString(R.string.size) + " " + StorageUtil.getSizeText(getValue().size)
                                + ", " + getString(R.string.seeds) + " " + getValue().seeds
                                + ", " + getString(R.string.peers) + " " + getValue().peers;
                    }

                    @Override
                    public void onItemChosen() {
                        torrentsButton.showSelectedItem(getPosition());
                        videoInfo.setTorrentPosition(getPosition());
                        checkIsDownloads();
                    }
                });
            }
            torrentsButton.setItems(items, videoInfo.getTorrentPosition());
            checkIsDownloads();
            torrentsButton.setVisibility(View.VISIBLE);
            downloadOpenBtn.setVisibility(View.VISIBLE);
            watchItNow.setVisibility(View.VISIBLE);
        } else {
            subtitlesButton.setVisibility(View.GONE);
            torrentsButton.setVisibility(View.GONE);
            downloadOpenBtn.setVisibility(View.GONE);
            watchItNow.setVisibility(View.GONE);
        }
    }

    protected DownloadInfo buildDownloadInfo(@NonNull Torrent torrent) {
        DownloadInfo info = new DownloadInfo();
        info.type = videoInfo.getVideoType();
        info.imdb = videoInfo.imdb;
        info.torrentUrl = torrent.url;
        info.torrentMagnet = torrent.magnet;
        info.fileName = torrent.file;
        info.posterUrl = videoInfo.posterMediumUrl;
        info.title = videoInfo.title;
        info.state = TorrentState.DOWNLOADING;
        info.size = torrent.size;
        return info;
    }

    private void checkIsDownloads() {
        Torrent torrent = videoInfo.getCurrentTorrent();
        if (torrent == null) {
            downloadOpenBtn.setVisibility(View.GONE);
        } else {
            String selection = Downloads._TORRENT_URL + "=\"" + torrent.url + "\"";
            Cursor cursor = Downloads.query(getActivity(), null, selection, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    isDownloads = true;
                    showOpenBtn();
                } else {
                    isDownloads = false;
                    showDownloadBtn();
                }
                cursor.close();
            }
        }
    }

    private void showDownloadBtn() {
        downloadOpenBtn.setBackgroundResource(R.drawable.download_btn_selector);
        downloadOpenBtn.setText(R.string.download);
        downloadOpenBtn.setOnClickListener(downloadListener);
        downloadOpenBtn.setVisibility(View.VISIBLE);
    }

    private void showOpenBtn() {
        downloadOpenBtn.setBackgroundResource(R.drawable.open_btn_selector);
        downloadOpenBtn.setText(R.string.open);
        downloadOpenBtn.setOnClickListener(openListener);
        downloadOpenBtn.setVisibility(View.VISIBLE);
    }

    private boolean checkFreeSpace(String path, long size) {
        long freeSpace = StorageUtil.getAvailableSpaceInBytes(path);
        if (freeSpace <= size) {
            new DialogFragment() {

                @NonNull
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setTitle(R.string.application_name);
                    builder.setMessage(R.string.no_free_space);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            StorageUtil.clearCacheDir();
                        }
                    });

                    return builder.create();
                }
            }.show(getFragmentManager(), "no_free_space_dialog_" + hashCode());
            return false;
        }
        return true;
    }

    private void startWatch(WatchInfo watchInfo) {
        if (watchDialog == null) {
            watchDialog = new WatchDialog();
        }
        watchDialog.show(getFragmentManager(), watchInfo);
    }

    private WatchInfo buildWatchInfo(Torrent torrent) {
        WatchInfo watchInfo = new WatchInfo();
        watchInfo.type = videoInfo.getVideoType();
        watchInfo.watchDir = StorageUtil.getCacheDirPath();
        watchInfo.torrentUrl = torrent.url;
        watchInfo.torrentMagnet = torrent.magnet;
        watchInfo.fileName = torrent.file;
        watchInfo.subtitlesProviders = videoInfo.getSubtitlesProviders();
        watchInfo.subtitles = subtitles;
        return watchInfo;
    }

    /*
    * Subtitles
    * */

    protected void restartSubtitlesLoader() {
        if (videoInfo.getTorrents() != null && videoInfo.getTorrents().size() > 0) {
            ArrayList<SubtitlesProvider> providers = videoInfo.getSubtitlesProviders();
            if (providers == null) {
                subtitles = new Subtitles();
                updateSubtitles();
            } else {
                Bundle data = new Bundle();
                data.putParcelableArrayList("providers", providers);
                if (getActivity() != null) {
                    getActivity().getSupportLoaderManager().restartLoader(0, data, subtitlesLoaderCallbacks);
                }
            }
        }
    }

    private void updateSubtitles() {
        if (subtitles == null) {
            subtitlesButton.setVisibility(View.GONE);
        } else {
            final List<ListItemEntity> items = new ArrayList<>();
            ListItemEntity.addItemToList(items, withoutSubtitlesItem);
            ListItemEntity.addItemToList(items, customSubtitlesItem);

            for (int i = Subtitles.START_INDEX; i < subtitles.getLanguages().size(); i++) {
                ListItemEntity.addItemToList(items, new ListItemEntity<Integer>(i) {
                    @Override
                    public String getName() {
                        return subtitles.getLanguages().get(getValue());
                    }

                    @Override
                    public void onItemChosen() {
                        subtitles.setPosition(getValue());
                        subtitlesButton.showSelectedItem(getPosition());
                    }
                });
            }
            subtitlesButton.post(new Runnable() {
                @Override
                public void run() {
                    subtitlesButton.setItems(items, subtitles.getPosition());
                    subtitlesButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private LoaderManager.LoaderCallbacks<Subtitles> subtitlesLoaderCallbacks = new LoaderManager.LoaderCallbacks<Subtitles>() {
        @Override
        public Loader<Subtitles> onCreateLoader(int id, Bundle args) {
            ArrayList<SubtitlesProvider> providers = args.getParcelableArrayList("providers");
            subtitlesButton.setVisibility(View.GONE);
            return new HttpProviderLoader<>(getActivity(), providers);
        }

        @Override
        public void onLoadFinished(Loader<Subtitles> loader, Subtitles data) {
            if (data == null) {
                subtitles = new Subtitles();
            } else {
                subtitles = data;
            }
            updateSubtitles();
        }

        @Override
        public void onLoaderReset(Loader<Subtitles> loader) {

        }
    };

    private ListItemEntity<String> withoutSubtitlesItem = new ListItemEntity<String>(SubtitlesUtils.WITHOUT_SUBTITLES) {
        @Override
        public String getName() {
            return getString(R.string.without_subtitle);
        }

        @Override
        public void onItemChosen() {
            subtitles.setPosition(Subtitles.WITHOUT_POSITION);
            subtitlesButton.showSelectedItem(getPosition());
        }
    };

    private ListItemEntity<String> customSubtitlesItem = new ListItemEntity<String>(SubtitlesUtils.CUSTOM_SUBTITLES) {
        @Override
        public String getName() {
            return getString(R.string.custom_subtitle);
        }

        @Override
        public void onItemChosen() {
            if (customSubtitleDialog == null) {
                customSubtitleDialog = new FileChooserDialog();
                customSubtitleDialog.setTitle(R.string.select_subtitle);
                customSubtitleDialog.setChooserListener(customSubtitleListener);
                customSubtitleDialog.setAcceptExtensions(SubtitlesRenderer.SUPPORTED_EXTENSIONS);
                customSubtitleDialog.setDenyFolderNames(new String[]{
                        StorageUtil.ROOT_FOLDER_NAME
                });
            }
            if (!customSubtitleDialog.isAdded()) {
                customSubtitleDialog.show(getActivity().getSupportFragmentManager(), StorageUtil.getSDCardFolder(getActivity()));
            }
        }
    };

    private FileChooserDialog.OnChooserListener customSubtitleListener = new FileChooserDialog.OnChooserListener() {
        @Override
        public void onChooserSelected(File file) {
            subtitles.setCustom(Uri.fromFile(file).toString());
            subtitles.setPosition(Subtitles.CUSTOM_POSITION);
            subtitlesButton.showSelectedItem(customSubtitlesItem.getPosition());
            sendCustomSubtitle(file);
        }

        @Override
        public void onChooserCancel() {

        }
    };

	/*
     * Listeners
	 */

    private OnClickListener downloadListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            handler.sendEmptyMessage(HANDLER_DOWNLOAD);
        }
    };

    private OnClickListener openListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (videoInfo.getCurrentTorrent() != null) {
                Intent intent = new Intent(getActivity(), DownloadsActivity.class);
                intent.putExtra(DownloadsActivity.VIDEO_URL, videoInfo.getCurrentTorrent().url);
                startActivityForResult(intent, DOWNLOADS_REQUEST_CODE);
            }
        }
    };

    private OnClickListener watchItNowListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isDownloads) {
                handler.sendEmptyMessage(HANDLER_WATCH_DOWNLOAD);
            } else {
                handler.sendEmptyMessage(HANDLER_WATCH);
            }
        }
    };

	/*
     * Handler
	 */

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_DOWNLOAD:
                    handleDownload();
                    return true;
                case HANDLER_WATCH_DOWNLOAD:
                    handleWatchDownload();
                    return true;
                case HANDLER_WATCH:
                    handleWatch();
                    return true;
                default:
                    return false;
            }
        }
    });

    private void handleDownload() {
        if (!NetworkUtil.hasAvailableConnection(getActivity())) {
            showOptionDialog(getString(R.string.download), getString(R.string.disable_wifi_message), notHaveConnectionListener);
            return;
        }
        if (StorageUtil.getDownloadsDir() == null) {
            Toast.makeText(getActivity(), R.string.cache_folder_not_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        final Torrent torrent = videoInfo.getCurrentTorrent();
        if (torrent == null) {
            return;
        }
        if (!checkFreeSpace(StorageUtil.getDownloadsDirPath(), torrent.size)) {
            return;
        }

        if (VpnManager.getInstance().isHaveProviders()
                && Prefs.getPopcornPrefs().get(PopcornPrefs.CHECK_VPN_CONNECTION, PopcornPrefs.DEFAULT_CHECK_VPN_CONNECTION)
                && !VpnManager.getInstance().isConnected()) {
            new VpnDialog().show(getFragmentManager(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (DialogInterface.BUTTON_NEGATIVE == which) {
                        download(torrent);
                    }
                }
            });
            return;
        }
        download(torrent);
    }

    private void download(Torrent torrent) {
        DownloadInfo info = buildDownloadInfo(torrent);
        String uuid = UUID.randomUUID().toString();
        String directoryPath = StorageUtil.getDownloadsDirPath() + "/" + uuid;
        info.directory = new File(directoryPath);
        if (info.directory.exists()) {
            StorageUtil.clearDir(info.directory);
        } else {
            if (!info.directory.mkdirs()) {
                Logger.error("VideoBaseFragment: Cannot crate dir - " + info.directory.getAbsolutePath());
                return;
            }
        }
        isDownloads = true;
        showOpenBtn();
        downloadsClient.downloadsAdd(info, subtitles);
    }

    private void handleWatchDownload() {
        Torrent torrent = videoInfo.getCurrentTorrent();
        if (torrent == null) {
            return;
        }
        String selection = Downloads._TORRENT_URL + "=\"" + torrent.url + "\"";
        Cursor cursor = Downloads.query(getActivity(), null, selection, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            try {
                DownloadInfo downloadInfo = new DownloadInfo();
                downloadInfo.populate(cursor);
                cursor.close();
                startWatch(new WatchInfo(downloadInfo, subtitles));
            } catch (Exception e) {
                Logger.error("VideoBaseFragment<handleWatchDownload>: error", e);
            }
        } else {
            isDownloads = false;
            showDownloadBtn();
        }
    }

    private void handleWatch() {
        if (!NetworkUtil.hasAvailableConnection(getActivity())) {
            showOptionDialog(getString(R.string.watch), getString(R.string.disable_wifi_message), notHaveConnectionListener);
            return;
        }
        if (StorageUtil.getCacheDir() == null) {
            Toast.makeText(getActivity(), R.string.cache_folder_not_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        final Torrent torrent = videoInfo.getCurrentTorrent();
        if (torrent != null) {
            if (VpnManager.getInstance().isHaveProviders()
                    && Prefs.getPopcornPrefs().get(PopcornPrefs.CHECK_VPN_CONNECTION, PopcornPrefs.DEFAULT_CHECK_VPN_CONNECTION)
                    && !VpnManager.getInstance().isConnected()) {
                new VpnDialog().show(getFragmentManager(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (DialogInterface.BUTTON_NEGATIVE == which) {
                            watch(torrent);
                        }
                    }
                });
                return;
            }
            watch(torrent);
        }
    }

    private void watch(@NonNull Torrent torrent) {
        String lastTorrent = Prefs.getPopcornPrefs().get(PopcornPrefs.LAST_TORRENT, "");
        if (TextUtils.isEmpty(lastTorrent)) {
            if (checkFreeSpace(StorageUtil.getCacheDirPath(), torrent.size)) {
                startWatch(buildWatchInfo(torrent));
            }
        } else {
            if (lastTorrent.equals(torrent.url) || lastTorrent.equals(torrent.magnet)) {
                startWatch(buildWatchInfo(torrent));
            } else {
                downloadsClient.removeTorrent(lastTorrent);
                Prefs.getPopcornPrefs().put(PopcornPrefs.LAST_TORRENT, "");
                StorageUtil.clearCacheDir();
                if (checkFreeSpace(StorageUtil.getCacheDirPath(), torrent.size)) {
                    startWatch(buildWatchInfo(torrent));
                }
            }
        }
    }

	/*
     * Dialogs
	 */

    private void showOptionDialog(String title, String message, OptionDialog.OptionListener listener) {
        if (!optionDialog.isAdded()) {
            optionDialog.setArguments(OptionDialog.createArguments(title, message));
            optionDialog.setListener(listener);
            optionDialog.show(getFragmentManager(), "option_dialog");
        }
    }

    private OptionDialog.OptionListener notHaveConnectionListener = new OptionDialog.SimpleOptionListener() {
        @Override
        public boolean positiveShow() {
            return true;
        }

        @Override
        public String positiveButtonText() {
            return getString(android.R.string.ok);
        }
    };
}