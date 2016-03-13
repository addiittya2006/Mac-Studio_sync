package se.popcorn_time.base.providers.video.info.api;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import se.popcorn_time.base.model.video.info.CinemaTvShowsInfo;
import se.popcorn_time.base.parser.ApiParser;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;
import se.popcorn_time.base.utils.Logger;

public class ApiCinemaTvShowsInfoProvider extends VideoInfoProvider<CinemaTvShowsInfo> {

    final String BASE_PATH = "http://api.torrentsapi.com/show?imdb=";

    public ApiCinemaTvShowsInfoProvider(String imdb) {
        this.params = new String[]{imdb};
    }

    private ApiCinemaTvShowsInfoProvider(Parcel parcel) {
        super(parcel);
    }

    @Override
    public CinemaTvShowsInfo create() {
        return new CinemaTvShowsInfo();
    }

    @Override
    public String createPath(String[] params) {
        return BASE_PATH + params[0];
    }

    @Override
    public void populate(CinemaTvShowsInfo info, String s) {
        try {
            ApiParser.populateTorrents(info, new JSONObject(s));
        } catch (JSONException e) {
            Logger.error("ApiCinemaTvShowsInfoProvider<populate>: Error", e);
        }
    }

    public static final Creator<ApiCinemaTvShowsInfoProvider> CREATOR = new Creator<ApiCinemaTvShowsInfoProvider>() {
        @Override
        public ApiCinemaTvShowsInfoProvider createFromParcel(Parcel source) {
            return new ApiCinemaTvShowsInfoProvider(source);
        }

        @Override
        public ApiCinemaTvShowsInfoProvider[] newArray(int size) {
            return new ApiCinemaTvShowsInfoProvider[size];
        }
    };
}