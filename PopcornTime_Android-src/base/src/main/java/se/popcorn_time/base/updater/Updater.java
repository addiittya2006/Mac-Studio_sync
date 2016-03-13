package se.popcorn_time.base.updater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import se.popcorn_time.base.utils.Logger;
import se.popcorn_time.base.utils.NetworkUtil;

public class Updater implements UpdaterReceiver {

    private static Updater INSTANCE = new Updater();

    public static final int RESULT_CURRENT_VERSION = 1001;
    public static final int RESULT_HAVE_UPDATE = 1002;

    public static final String APK_URI = "apk-uri";
    public static final String APP_VERSION = "app-version";

    private UpdaterResultReceiver resultReceiver;
    private UpdaterProvider updaterProvider;
    private Intent intent;
    private Activity mActivity;
    private boolean haveUpdate = false;
    private Bundle updaterArgs;

    private UpdaterDialog updaterDialog;

    private Updater() {
        resultReceiver = new UpdaterResultReceiver(new Handler());
        resultReceiver.setReceiver(Updater.this);
    }

    public static Updater getInstance() {
        return INSTANCE;
    }

    public static void init(UpdaterProvider provider) {
        INSTANCE.updaterProvider = provider;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (RESULT_HAVE_UPDATE == resultCode) {
            if (mActivity != null) {
                showUpdaterDialog(resultData);
            } else {
                haveUpdate = true;
                updaterArgs = resultData;
            }
        }
        intent = null;
    }

    public void setCurrentActivity(Activity activity) {
        this.mActivity = activity;
        if (haveUpdate) {
            showUpdaterDialog(updaterArgs);
            haveUpdate = false;
        }
    }

    public void checkUpdate(Context context) {
        if (updaterProvider == null) {
            Logger.error("Updater<checkUpdate>: Updater not init.");
            return;
        }
        if (!NetworkUtil.hasAvailableConnection(context)) {
            Logger.error("Updater<checkUpdate>: Not have available connection.");
            return;
        }
        if (intent != null) {
            Logger.error("Updater<checkUpdate>: Already running.");
            return;
        }

        intent = new Intent(Intent.ACTION_SYNC, null, context, UpdaterService.class);
        intent.putExtra(UpdaterService.RESULT_RECEIVER, resultReceiver);
        intent.putExtra(UpdaterService.UPDATER_PROVIDER, updaterProvider);
        context.startService(intent);
    }

    public void stopUpdate(Context context) {
        if (intent != null) {
            context.stopService(intent);
            intent = null;
        }
    }

    private void showUpdaterDialog(Bundle args) {
        if (updaterDialog == null) {
            updaterDialog = new UpdaterDialog();
        }
        if (mActivity != null && !updaterDialog.isAdded()) {
            updaterDialog.setArguments(args);
            updaterDialog.show(mActivity.getFragmentManager(), "update_dialog");
        }
    }
}