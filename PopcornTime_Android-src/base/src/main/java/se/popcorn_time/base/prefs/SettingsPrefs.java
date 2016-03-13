package se.popcorn_time.base.prefs;

import android.content.Context;

public class SettingsPrefs extends BasePrefs {

    public static final String START_PAGE = "start-page";
    public static final String ROOT_FOLDER_PATH = "chache-folder-path";
    public static final String HARDWARE_ACCELERATION = "hardware-acceleration";
    public static final String CLEAR_ON_EXIT = "clear-on-exit";
    public static final String ONLY_WIFI_CONNECTION = "only-wifi-connection";
    public static final String MAXIMUM_DOWNLOAD_SPEED = "maximum-download-speed";
    public static final String MAXIMUM_UPLOAD_SPEED = "maximum-upload-speed";

    public static final String SUBTITLE_LANGUAGE = "subtitle-language";
    public static final String SUBTITLE_FONT_SIZE = "subtitle-font-size";
    public static final String SUBTITLE_FONT_COLOR = "subtitle-font-color";

    public SettingsPrefs(Context context) {
        super(context);
    }

    @Override
    protected String getPrefsName() {
        return "PopcornPreferences";
    }

}