package se.popcorn_time.base.providers.subtitles;

import android.os.Parcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.prefs.SettingsPrefs;
import se.popcorn_time.base.subtitles.Subtitles;
import se.popcorn_time.base.subtitles.SubtitlesLanguage;
import se.popcorn_time.base.utils.Logger;

public class YifiSubtitlesProvider extends SubtitlesProvider {

    final String BASE_PATH = "http://api.yifysubtitles.com/subs/";

    public YifiSubtitlesProvider(String imdb) {
        this.params = new String[]{imdb};
    }

    private YifiSubtitlesProvider(Parcel parcel) {
        super(parcel);
    }

    @Override
    public String createPath(String[] params) {
        return BASE_PATH + params[0];
    }

    @Override
    public void populate(Subtitles subtitles, String s) {
        try {
            JSONObject jsonResponse = new JSONObject(s);
            int count = jsonResponse.getInt("subtitles");
            if (count > 0) {
                JSONObject jsonSubtitles = jsonResponse.getJSONObject("subs").getJSONObject(params[0]);
                Iterator<String> iterator = jsonSubtitles.keys();
                String subLang = Prefs.getSettingsPrefs().get(SettingsPrefs.SUBTITLE_LANGUAGE, SubtitlesLanguage.DEFAULT_SUBTITLE_LANGUAGE);
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    JSONArray subInfos = jsonSubtitles.getJSONArray(key);
                    int subRating = Integer.MIN_VALUE;
                    String subUrl = "";
                    for (int i = 0; i < subInfos.length(); i++) {
                        JSONObject subInfo = subInfos.getJSONObject(i);
                        int rating = subInfo.getInt("rating");
                        if (rating > subRating) {
                            subRating = rating;
                            subUrl = subInfo.getString("url");
                        }
                    }
                    if (!"".equals(subUrl)) {
                        if (subtitles.add(SubtitlesLanguage.subtitlesNameToNative(key), "http://www.yifysubtitles.com" + subUrl) && subLang.equals(key)) {
                            subtitles.setPosition(subtitles.getUrls().size() - 1);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Logger.error("YifiSubtitlesProvider<populate>: Error", e);
        }
    }

    public static final Creator<YifiSubtitlesProvider> CREATOR = new Creator<YifiSubtitlesProvider>() {
        @Override
        public YifiSubtitlesProvider createFromParcel(Parcel source) {
            return new YifiSubtitlesProvider(source);
        }

        @Override
        public YifiSubtitlesProvider[] newArray(int size) {
            return new YifiSubtitlesProvider[size];
        }
    };
}