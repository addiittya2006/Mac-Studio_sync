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
import se.popcorn_time.base.model.video.info.AnimeMoviesInfo;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.parser.ApiParser;
import se.popcorn_time.base.providers.video.VideoFilter;
import se.popcorn_time.base.providers.video.list.VideoListProvider;
import se.popcorn_time.base.utils.Logger;

public class ApiAnimeMoviesListProvider extends VideoListProvider {

    final String BASE_PATH = "http://api.anime.torrentsapi.com/" + SubcategoryType.MOVIES + "?cb=" + new Random().nextDouble();

    public ApiAnimeMoviesListProvider() {
        videoFilter = new ApiAnimeMoviesFilter();
    }

    private ApiAnimeMoviesListProvider(Parcel parcel) {
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
                AnimeMoviesInfo info = new AnimeMoviesInfo();
                ApiParser.populate(info, jsonMovie);
                videoInfos.add(info);
            }
        } catch (JSONException e) {
            Logger.error("ApiAnimeMoviesProvider<populate>: Error", e);
        }
    }

    private class ApiAnimeMoviesFilter extends VideoFilter {
        public ApiAnimeMoviesFilter() {
            genre.getRequestParams().add(R.string.popular, "");

            sortBy = ApiSortBy.getInstance();

            quality.getRequestParams().add(R.string.quality, "720p,1080p");
        }
    }

    public static final Parcelable.Creator<ApiAnimeMoviesListProvider> CREATOR = new Parcelable.Creator<ApiAnimeMoviesListProvider>() {
        @Override
        public ApiAnimeMoviesListProvider createFromParcel(Parcel source) {
            return new ApiAnimeMoviesListProvider(source);
        }

        @Override
        public ApiAnimeMoviesListProvider[] newArray(int size) {
            return new ApiAnimeMoviesListProvider[size];
        }
    };
}