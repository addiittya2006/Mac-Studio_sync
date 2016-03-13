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
import se.popcorn_time.base.model.video.info.CinemaTvShowsInfo;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.parser.ApiParser;
import se.popcorn_time.base.providers.video.VideoFilter;
import se.popcorn_time.base.providers.video.list.VideoListProvider;
import se.popcorn_time.base.utils.Logger;

public class ApiCinemaTvShowsListProvider extends VideoListProvider {

    final String BASE_PATH = "http://api.torrentsapi.com/" + SubcategoryType.TV_SHOWS + "?cb=" + new Random().nextDouble();

    public ApiCinemaTvShowsListProvider() {
        videoFilter = new ApiCinemaTvShowsFilter();
    }

    private ApiCinemaTvShowsListProvider(Parcel parcel) {
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
            JSONArray jsonTvShows = new JSONObject(s).getJSONArray("MovieList");
            for (int i = 0; i < jsonTvShows.length(); i++) {
                JSONObject jsonTvShow = jsonTvShows.getJSONObject(i);
                CinemaTvShowsInfo info = new CinemaTvShowsInfo();
                ApiParser.populate(info, jsonTvShow);
                videoInfos.add(info);
            }
        } catch (JSONException e) {
            Logger.error("ApiCinemaTvShowsProvider<populate>: Error", e);
        }
    }

    private class ApiCinemaTvShowsFilter extends VideoFilter {
        public ApiCinemaTvShowsFilter() {
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
        }
    }

    public static final Parcelable.Creator<ApiCinemaTvShowsListProvider> CREATOR = new Parcelable.Creator<ApiCinemaTvShowsListProvider>() {
        @Override
        public ApiCinemaTvShowsListProvider createFromParcel(Parcel source) {
            return new ApiCinemaTvShowsListProvider(source);
        }

        @Override
        public ApiCinemaTvShowsListProvider[] newArray(int size) {
            return new ApiCinemaTvShowsListProvider[size];
        }
    };
}