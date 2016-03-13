package se.popcorn_time.base.providers.video.info.api;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import se.popcorn_time.base.model.video.info.AnimeTvShowsInfo;
import se.popcorn_time.base.parser.ApiParser;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;
import se.popcorn_time.base.utils.Logger;

public class ApiAnimeTvShowsInfoProvider extends VideoInfoProvider<AnimeTvShowsInfo> {

    final String BASE_PATH = "http://api.anime.torrentsapi.com/show?imdb=";

    public ApiAnimeTvShowsInfoProvider(String imdb) {
        this.params = new String[]{imdb};
    }

    private ApiAnimeTvShowsInfoProvider(Parcel parcel) {
        super(parcel);
    }

    @Override
    public AnimeTvShowsInfo create() {
        return null;
    }

    @Override
    public String createPath(String[] params) {
        return BASE_PATH + params[0];
    }

    @Override
    public void populate(AnimeTvShowsInfo info, String s) {
        try {
            ApiParser.populateTorrents(info, new JSONObject(s));
        } catch (JSONException e) {
            Logger.error("ApiAnimeTvShowsInfoProvider<populate>: Error", e);
        }
    }

    public static final Creator<ApiAnimeTvShowsInfoProvider> CREATOR = new Creator<ApiAnimeTvShowsInfoProvider>() {
        @Override
        public ApiAnimeTvShowsInfoProvider createFromParcel(Parcel source) {
            return new ApiAnimeTvShowsInfoProvider(source);
        }

        @Override
        public ApiAnimeTvShowsInfoProvider[] newArray(int size) {
            return new ApiAnimeTvShowsInfoProvider[size];
        }
    };
}