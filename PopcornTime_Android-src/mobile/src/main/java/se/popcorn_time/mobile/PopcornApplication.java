package se.popcorn_time.mobile;

import android.app.Application;
import android.content.Intent;
import android.support.multidex.MultiDex;

import dp.ws.popcorntime.BuildConfig;
import dp.ws.popcorntime.R;
import se.popcorn_time.base.prefs.PopcornPrefs;
import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.storage.StorageUtil;
import se.popcorn_time.base.subtitles.SubtitlesLanguage;
import se.popcorn_time.base.utils.InterfaceUtil;
import se.popcorn_time.base.utils.Logger;
import se.popcorn_time.base.vpn.VpnManager;
import se.popcorn_time.mobile.ui.MainActivity;

public class PopcornApplication extends Application {

    @Override
    public void onCreate() {
        MultiDex.install(getBaseContext());
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Logger.init("pt_mobile");
        }

        Prefs.init(getBaseContext());

        VpnManager.getInstance().loadProviders();

        StorageUtil.init(getBaseContext());
        InterfaceUtil.init(getResources());
        SubtitlesLanguage.init(getResources());

        addShortcut();
    }

    private void addShortcut() {
        if (!Prefs.getPopcornPrefs().get(PopcornPrefs.IS_SHORTCUT_CREATED, false)) {
            Intent shortcutIntent = new Intent(getApplicationContext(), MainActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.application_name));
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher));
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            getApplicationContext().sendBroadcast(addIntent);
            Prefs.getPopcornPrefs().put(PopcornPrefs.IS_SHORTCUT_CREATED, true);
        }
    }
}