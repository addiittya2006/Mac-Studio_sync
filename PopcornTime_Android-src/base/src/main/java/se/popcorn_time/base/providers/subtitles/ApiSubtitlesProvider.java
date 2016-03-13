package se.popcorn_time.base.providers.subtitles;

import android.os.Parcel;

import com.player.subtitles.FormatSRT;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.prefs.SettingsPrefs;
import se.popcorn_time.base.subtitles.Subtitles;
import se.popcorn_time.base.subtitles.SubtitlesLanguage;
import se.popcorn_time.base.utils.Logger;

public class ApiSubtitlesProvider extends SubtitlesProvider {

    final String BASE_PATH = "http://sub.torrentsapi.com/list?imdb=";

    public ApiSubtitlesProvider(String imdb) {
        this.params = new String[]{imdb};
    }

    public ApiSubtitlesProvider(String imdb, int season, int episode) {
        this.params = new String[]{imdb, Integer.toString(season), Integer.toString(episode)};
        //  "&e=" + (getCurrentEpisode().getNumber() - 1);
        //  "&ep=" + getCurrentEpisode().getNumber();
    }

    private ApiSubtitlesProvider(Parcel parcel) {
        super(parcel);
    }

    @Override
    public String createPath(String[] params) {
        if (params.length == 1) {
            return BASE_PATH + params[0];
        } else if (params.length == 3) {
            return BASE_PATH + params[0] + "&s=" + params[1] + "&ep=" + params[2];
        }
        return null;
    }

    @Override
    public void populate(Subtitles subtitles, String s) {
        try {
            JSONObject jsonResponse = new JSONObject(s);
            int count = jsonResponse.getInt("subtitles");
            if (count > 0) {
                JSONObject jsonSubtitles = jsonResponse.getJSONObject("subs");
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
                        String format = subInfo.getString("format");
                        if (rating > subRating && FormatSRT.EXTENSION.equals(format)) {
                            subRating = rating;
                            subUrl = subInfo.getString("url");
                        }
                    }
                    if (!"".equals(subUrl)) {
                        key = SubtitlesLanguage.subtitlesIsoToName(key);
                        if (subtitles.add(SubtitlesLanguage.subtitlesNameToNative(key), subUrl) && subLang.equals(key)) {
                            subtitles.setPosition(subtitles.getUrls().size() - 1);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Logger.error("ApiSubtitlesProvider<populate>: Error", e);
        }
    }

    public static final Creator<ApiSubtitlesProvider> CREATOR = new Creator<ApiSubtitlesProvider>() {
        @Override
        public ApiSubtitlesProvider createFromParcel(Parcel source) {
            return new ApiSubtitlesProvider(source);
        }

        @Override
        public ApiSubtitlesProvider[] newArray(int size) {
            return new ApiSubtitlesProvider[size];
        }
    };
}