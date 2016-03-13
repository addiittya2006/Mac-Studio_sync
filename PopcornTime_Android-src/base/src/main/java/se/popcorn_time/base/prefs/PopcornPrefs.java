package se.popcorn_time.base.prefs;

import android.content.Context;

public class PopcornPrefs extends BasePrefs {

    public static final String IS_SHORTCUT_CREATED = "is-shortcut-created";
    public static final String APP_LOCALE = "app-locale";
    public static final String UPDATE_APK_PATH = "update-apk-path";
    public static final String LAST_TORRENT = "last-torrent";

    public static final String VPN_PROVIDERS = "vpn-providers";
    public static final String ON_START_VPN_PACKAGE = "on-start-vpn-package";
    public static final String CHECK_VPN_CONNECTION = "check-vpn-connection";

    public static final boolean DEFAULT_CHECK_VPN_CONNECTION = true;

    public PopcornPrefs(Context context) {
        super(context);
    }

    @Override
    protected String getPrefsName() {
        return "PopcornPreferences";
    }

}