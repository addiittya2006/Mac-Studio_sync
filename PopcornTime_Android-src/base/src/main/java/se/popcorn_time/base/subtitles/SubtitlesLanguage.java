package se.popcorn_time.base.subtitles;

import android.content.res.Resources;

import java.util.HashMap;
import java.util.Locale;

import se.popcorn_time.base.R;
import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.prefs.SettingsPrefs;

public class SubtitlesLanguage {

    public static final String DEFAULT_SUBTITLE_LANGUAGE = "";
    public static final int POSITION_WITHOUT_SUBTITLES = 0;

    private static String[] subtitlesName;
    private static String[] subtitlesNative;
    private static String[] subtitlesIso;

    private static HashMap<String, String> subtitlesNativeByName;
    private static HashMap<String, String> subtitlesNameByIso;
    private static HashMap<String, String> subtitlesIsoByName;

    private SubtitlesLanguage() {

    }

    public static void init(Resources resources) {
        subtitlesName = resources.getStringArray(R.array.subtitles_name);
        subtitlesNative = resources.getStringArray(R.array.subtitles_native);
        subtitlesIso = resources.getStringArray(R.array.subtitles_iso);

        subtitlesNativeByName = new HashMap<>();
        int count = subtitlesName.length <= subtitlesNative.length ? subtitlesName.length : subtitlesNative.length;
        for (int i = 1; i < count; i++) {
            subtitlesNativeByName.put(subtitlesName[i], subtitlesNative[i]);
        }

        subtitlesNameByIso = new HashMap<>();
        count = subtitlesIso.length <= subtitlesName.length ? subtitlesIso.length : subtitlesName.length;
        for (int i = 1; i < count; i++) {
            subtitlesNameByIso.put(subtitlesIso[i], subtitlesName[i]);
        }

        subtitlesIsoByName = new HashMap<>();
        count = subtitlesName.length <= subtitlesNative.length ? subtitlesName.length : subtitlesNative.length;
        for (int i = 1; i < count; i++) {
            subtitlesIsoByName.put(subtitlesName[i], subtitlesIso[i]);
        }

        if (!Prefs.getSettingsPrefs().contains(SettingsPrefs.SUBTITLE_LANGUAGE)) {
            Prefs.getSettingsPrefs().put(SettingsPrefs.SUBTITLE_LANGUAGE, getDefaultSubtitlesLanguage());
        }
    }

    private static String getDefaultSubtitlesLanguage() {
        String language = Locale.getDefault().getLanguage();
        if (subtitlesNameByIso.containsKey(language)) {
            return subtitlesNameByIso.get(language);
        }
        return DEFAULT_SUBTITLE_LANGUAGE;
    }

    public static void setWithoutSubtitlesText(String text) {
        subtitlesNative[POSITION_WITHOUT_SUBTITLES] = text;
    }

    public static String[] getSubtitlesName() {
        return subtitlesName;
    }

    public static String[] getSubtitlesNative() {
        return subtitlesNative;
    }

    public static String subtitlesNameToNative(String name) {
        name = name.toLowerCase();
        if (subtitlesNativeByName.containsKey(name)) {
            return subtitlesNativeByName.get(name);
        }
        return name;
    }

    public static String subtitlesIsoToName(String iso) {
        iso = iso.toLowerCase();
        if (subtitlesNameByIso.containsKey(iso)) {
            return subtitlesNameByIso.get(iso);
        }
        return iso;
    }
}