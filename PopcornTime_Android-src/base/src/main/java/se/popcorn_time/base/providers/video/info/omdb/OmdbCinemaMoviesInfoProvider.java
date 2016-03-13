package se.popcorn_time.base.providers.video.info.omdb;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import se.popcorn_time.base.model.video.info.CinemaMoviesInfo;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;
import se.popcorn_time.base.utils.JSONHelper;
import se.popcorn_time.base.utils.Logger;

public class OmdbCinemaMoviesInfoProvider extends VideoInfoProvider<CinemaMoviesInfo> {

    final String BASE_PATH = "http://www.omdbapi.com/?i=";

    public OmdbCinemaMoviesInfoProvider(String imdb) {
        this.params = new String[]{imdb};
    }

    private OmdbCinemaMoviesInfoProvider(Parcel parcel) {
        super(parcel);
    }

    @Override
    public CinemaMoviesInfo create() {
        return new CinemaMoviesInfo();
    }

    @Override
    public String createPath(String[] params) {
        return BASE_PATH + params[0];
    }

    @Override
    public void populate(CinemaMoviesInfo info, String s) {
        if (info != null) {
            try {
                JSONObject jsonInfo = new JSONObject(s);
                String act = JSONHelper.getString(jsonInfo, "Actors", "");
                if (act.length() > 3) {
                    info.actors = act;
                }
                String desc = JSONHelper.getString(jsonInfo, "Plot", "");
                if (desc.length() > 3) {
                    info.description = desc;
                }
                info.description += "<br><b>" + JSONHelper.getString(jsonInfo, "Country", "") + ". "
                        + JSONHelper.getString(jsonInfo, "Runtime", "90m") + ". "
                        + JSONHelper.getString(jsonInfo, "Year", "") + "</b>";
            } catch (JSONException e) {
                Logger.error("OmdbCinemaMoviesInfoProvider<populate>: Error", e);
            }
        }
    }

    public static final Creator<OmdbCinemaMoviesInfoProvider> CREATOR = new Creator<OmdbCinemaMoviesInfoProvider>() {
        @Override
        public OmdbCinemaMoviesInfoProvider createFromParcel(Parcel source) {
            return new OmdbCinemaMoviesInfoProvider(source);
        }

        @Override
        public OmdbCinemaMoviesInfoProvider[] newArray(int size) {
            return new OmdbCinemaMoviesInfoProvider[size];
        }
    };
}