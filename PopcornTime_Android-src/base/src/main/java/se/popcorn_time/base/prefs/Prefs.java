package se.popcorn_time.base.prefs;

import android.content.Context;

public final class Prefs {

    private final static Prefs INSTANCE = new Prefs();

    private BasePrefs popcornPrefs;
    private BasePrefs settingsPrefs;

    private Prefs() {

    }

    public static void init(Context context) {
        INSTANCE.popcornPrefs = new PopcornPrefs(context);
        INSTANCE.settingsPrefs = new SettingsPrefs(context);
    }

    public static BasePrefs getPopcornPrefs() {
        return INSTANCE.popcornPrefs;
    }

    public static BasePrefs getSettingsPrefs() {
        return INSTANCE.settingsPrefs;
    }
}