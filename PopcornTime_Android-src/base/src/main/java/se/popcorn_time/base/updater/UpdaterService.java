package se.popcorn_time.base.updater;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import se.popcorn_time.base.prefs.PopcornPrefs;
import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.utils.Logger;

public class UpdaterService extends IntentService {

    public static final String RESULT_RECEIVER = "result-receiver";
    public static final String UPDATER_PROVIDER = "updater-provider";

    private DownloadManager downloadManager;
    private long downloadId;
    private boolean isCancelled;

    public UpdaterService() {
        super(UpdaterService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isCancelled = false;
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isCancelled = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);
        if (resultReceiver != null) {
            try {
                UpdaterProvider provider = intent.getParcelableExtra(UPDATER_PROVIDER);
                String url = provider.getPath();
                Logger.debug("Updater: " + url);
                OkHttpClient httpClient = new OkHttpClient();
                Response response = httpClient.newCall(new Request.Builder().url(url).build()).execute();
                if (response.isSuccessful()) {
                    String _response = response.body().string();
                    Logger.debug("Updater response: " + _response);
                    if (TextUtils.isEmpty(_response)) {
                        currentVersion(resultReceiver);
                    } else {
                        UpdaterInfo info = provider.create();
                        provider.populate(info, _response);
                        if (TextUtils.isEmpty(info.downloadUrl) || TextUtils.isEmpty(info.version)) {
                            currentVersion(resultReceiver);
                        } else {
                            newVersion(info, resultReceiver);
                        }
                    }
                }
            } catch (IOException e) {
                Logger.error("UpdaterService<onHandleIntent>: Error", e);
            }
        }
    }

    private void deleteApk(File file) {
        if (file != null && file.exists() && file.delete()) {
            Logger.debug("UpdaterService<deleteApk>: " + file.getAbsolutePath());
        }
        Prefs.getPopcornPrefs().put(PopcornPrefs.UPDATE_APK_PATH, "");
    }

    private void currentVersion(ResultReceiver resultReceiver) {
        Logger.debug("UpdaterService: Current version");
        String filePath = Prefs.getPopcornPrefs().get(PopcornPrefs.UPDATE_APK_PATH, "");
        if (!"".equals(filePath)) {
            deleteApk(new File(filePath));
        }
        resultReceiver.send(Updater.RESULT_CURRENT_VERSION, null);
    }

    private void newVersion(UpdaterInfo updaterInfo, ResultReceiver resultReceiver) {
        Logger.debug("UpdaterService: New version " + updaterInfo.version);
        String filePath = Prefs.getPopcornPrefs().get(PopcornPrefs.UPDATE_APK_PATH, "");
        File file = new File(getBaseContext().getExternalFilesDir(null), "popcorntime-" + updaterInfo.version + ".apk");
        Logger.debug(file.getAbsolutePath());
        if (file.exists() && file.getAbsolutePath().equals(filePath)) {
            downloadComplete(resultReceiver, file, updaterInfo.version);
        } else {
            deleteApk(file);
            download(resultReceiver, updaterInfo, file);
        }
    }

    private void download(ResultReceiver resultReceiver, UpdaterInfo updaterInfo, File file) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(updaterInfo.downloadUrl));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setDestinationUri(Uri.parse("file://" + file.getAbsolutePath()));
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        try {
            downloadId = downloadManager.enqueue(request);
        } catch (Exception ex) {
            downloadId = -1;
        }

        if (downloadId == -1) {
            downloadURL(resultReceiver, updaterInfo, file);
        } else {
            downloadManager(resultReceiver, updaterInfo, file);
        }
    }

    private void downloadURL(ResultReceiver resultReceiver, UpdaterInfo updaterInfo, File file) {
        InputStream input = null;
        OutputStream output = null;
        while (true) {
            try {
                URL url = new URL(updaterInfo.downloadUrl);
                URLConnection connection = url.openConnection();
                connection.connect();
                input = new BufferedInputStream(connection.getInputStream());
                output = new FileOutputStream(file);

                byte data[] = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCancelled) {
                        break;
                    }
                    if (count != 0) {
                        output.write(data, 0, count);
                    }
                }
                if (isCancelled) {
                    deleteApk(file);
                    break;
                }
                output.flush();
                downloadComplete(resultReceiver, file, updaterInfo.version);
                break;
            } catch (Exception e) {
                Logger.error("UpdaterService<download>: Error", e);
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (Exception ex) {
                    // close exception
                }
            }
        }
    }

    private void downloadManager(ResultReceiver resultReceiver, UpdaterInfo updaterInfo, File file) {
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException ex) {
                // interrupt
            } finally {
                if (isCancelled) {
                    downloadManager.remove(downloadId);
                }
            }
            Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId));
            if (cursor != null) {
                int status = DownloadManager.STATUS_FAILED;
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                }
                cursor.close();
                switch (status) {
                    case DownloadManager.STATUS_SUCCESSFUL:
                        downloadComplete(resultReceiver, file, updaterInfo.version);
                        return;
                    case DownloadManager.STATUS_FAILED:
                        return;
                    default:
                        break;
                }
            }
        }
    }

    private void downloadComplete(ResultReceiver resultReceiver, File file, String version) {
        Prefs.getPopcornPrefs().put(PopcornPrefs.UPDATE_APK_PATH, file.getAbsolutePath());
        Bundle data = new Bundle();
        data.putString(Updater.APK_URI, "file://" + file.getAbsolutePath());
        data.putString(Updater.APP_VERSION, version);
        resultReceiver.send(Updater.RESULT_HAVE_UPDATE, data);
    }
}