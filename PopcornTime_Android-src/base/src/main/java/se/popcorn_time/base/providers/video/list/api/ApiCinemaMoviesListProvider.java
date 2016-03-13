package se.popcorn_time.base.providers.video.list.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import se.popcorn_time.base.R;
import se.popcorn_time.base.model.content.subcategory.SubcategoryType;
import se.popcorn_time.base.model.video.info.CinemaMoviesInfo;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.parser.ApiParser;
import se.popcorn_time.base.providers.video.VideoFilter;
import se.popcorn_time.base.providers.video.list.VideoListProvider;
import se.popcorn_time.base.utils.Logger;

public class ApiCinemaMoviesListProvider extends VideoListProvider {

    final String BASE_PATH = "http://api.torrentsapi.com/" + SubcategoryType.MOVIES + "?cb=" + new Random().nextDouble();

    public ApiCinemaMoviesListProvider() {
        videoFilter = new ApiCinemaMoviesFilter();
    }

    private ApiCinemaMoviesListProvider(Parcel parcel) {
        super(parcel);
    }

    @Override
    public void updateParams(String keywords) {
        String sortBy = videoFilter != null ? videoFilter.getSortBy().getCurrentRequestParam() : null;
        String genre = TextUtils.isEmpty(keywords) ? (videoFilter != null ? videoFilter.getGenre().getCurrentRequestParam() : null) : null;
        String quality = videoFilter != null ? videoFilter.getQuality().getCurrentRequestParam() : null;
        this.params = new String[]{sortBy, genre, quality, keywords};
        this.page = 1;
    }

    @Override
    public String createPath(String[] params) {
        String path = BASE_PATH;
        if (!TextUtils.isEmpty(params[0])) {
            path += "&sort=" + params[0];
        }
        if (!TextUtils.isEmpty(params[1])) {
            path += "&genre=" + params[1];
        }
        if (!TextUtils.isEmpty(params[2])) {
            path += "&quality=" + params[2];
        }
        if (!TextUtils.isEmpty(params[3])) {
            path += "&keywords=" + params[3];
        }
        path += "&page=" + page;
        return path;
    }

    @Override
    public void populate(ArrayList<VideoInfo> videoInfos, String s) {
        try {
            JSONArray jsonMovies = new JSONObject(s).getJSONArray("MovieList");
            for (int i = 0; i < jsonMovies.length(); i++) {
                JSONObject jsonMovie = jsonMovies.getJSONObject(i);
                CinemaMoviesInfo info = new CinemaMoviesInfo();
                ApiParser.populate(info, jsonMovie);
                videoInfos.add(info);
            }
        } catch (JSONException e) {
            Logger.error("ApiCinemaMoviesProvider<populate>: Error", e);
        }
    }

    private class ApiCinemaMoviesFilter extends VideoFilter {
        public ApiCinemaMoviesFilter() {
            genre.getRequestParams().add(R.string.popular, "");
            genre.getRequestParams().add(R.string.action, "action");
            genre.getRequestParams().add(R.string.adventure, "adventure");
            genre.getRequestParams().add(R.string.animation, "animation");
            genre.getRequestParams().add(R.string.biography, "biography");
            genre.getRequestParams().add(R.string.comedy, "comedy");
            genre.getRequestParams().add(R.string.crime, "crime");
            genre.getRequestParams().add(R.string.documentary, "documentary");
            genre.getRequestParams().add(R.string.drama, "drama");
            genre.getRequestParams().add(R.string.family, "family");
            genre.getRequestParams().add(R.string.fantasy, "fantasy");
            genre.getRequestParams().add(R.string.film_noir, "film-noir");
            genre.getRequestParams().add(R.string.history, "history");
            genre.getRequestParams().add(R.string.horror, "horror");
            genre.getRequestParams().add(R.string.music, "music");
            genre.getRequestParams().add(R.string.musical, "musical");
            genre.getRequestParams().add(R.string.mystery, "mystery");
            genre.getRequestParams().add(R.string.romance, "romance");
            genre.getRequestParams().add(R.string.sci_fi, "sci-fi");
            genre.getRequestParams().add(R.string.short_, "short");
            genre.getRequestParams().add(R.string.sport, "sport");
            genre.getRequestParams().add(R.string.thriller, "thriller");
            genre.getRequestParams().add(R.string.war, "war");
            genre.getRequestParams().add(R.string.western, "western");

            sortBy = ApiSortBy.getInstance();

            quality.getRequestParams().add(R.string.quality, "720p,1080p");
        }
    }

    public static final Parcelable.Creator<ApiCinemaMoviesListProvider> CREATOR = new Parcelable.Creator<ApiCinemaMoviesListProvider>() {
        @Override
        public ApiCinemaMoviesListProvider createFromParcel(Parcel source) {
            return new ApiCinemaMoviesListProvider(source);
        }

        @Override
        public ApiCinemaMoviesListProvider[] newArray(int size) {
            return new ApiCinemaMoviesListProvider[size];
        }
    };
}