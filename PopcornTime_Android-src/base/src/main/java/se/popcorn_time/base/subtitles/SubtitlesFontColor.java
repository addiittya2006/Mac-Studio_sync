package se.popcorn_time.base.subtitles;

import android.graphics.Color;

import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.prefs.SettingsPrefs;

public final class SubtitlesFontColor {

    public static final String WHITE = "#ffffff";
    public static final String YELLOW = "#ffff00";

    public static final String DEFAULT_COLOR = WHITE;
    public static final String[] COLORS = new String[]{WHITE, YELLOW};

    private SubtitlesFontColor() {

    }

    public static int getColor() {
        return Color.parseColor(Prefs.getSettingsPrefs().get(SettingsPrefs.SUBTITLE_FONT_COLOR, DEFAULT_COLOR));
    }
}