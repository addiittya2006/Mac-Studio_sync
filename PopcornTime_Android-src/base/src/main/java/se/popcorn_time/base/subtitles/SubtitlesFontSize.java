package se.popcorn_time.base.subtitles;

import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.prefs.SettingsPrefs;

public final class SubtitlesFontSize {

    public static final float EXTRA_SMALL = 0.7f;
    public static final float SMALL = 0.85f;
    public static final float NORMAL = 1f;
    public static final float LARGE = 1.25f;
    public static final float EXTRA_LARGE = 1.5f;

    public static final float DEFAULT_SIZE = NORMAL;
    public static final float[] SIZES = new float[]{EXTRA_SMALL, SMALL, NORMAL, LARGE, EXTRA_LARGE};

    private SubtitlesFontSize() {

    }

    public static float getSize() {
        return Prefs.getSettingsPrefs().get(SettingsPrefs.SUBTITLE_FONT_SIZE, DEFAULT_SIZE);
    }
}