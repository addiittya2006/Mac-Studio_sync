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
import se.popcorn_time.base.model.video.info.AnimeTvShowsInfo;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.parser.ApiParser;
import se.popcorn_time.base.providers.video.VideoFilter;
import se.popcorn_time.base.providers.video.list.VideoListProvider;
import se.popcorn_time.base.utils.Logger;

public class ApiAnimeTvShowsListProvider extends VideoListProvider {

    final String BASE_PATH = "http://api.anime.torrentsapi.com/" + SubcategoryType.TV_SHOWS + "?cb=" + new Random().nextDouble();

    public ApiAnimeTvShowsListProvider() {
        videoFilter = new ApiAnimeTvShowsFilter();
    }

    private ApiAnimeTvShowsListProvider(Parcel parcel) {
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
                AnimeTvShowsInfo info = new AnimeTvShowsInfo();
                ApiParser.populate(info, jsonTvShow);
                videoInfos.add(info);
            }
        } catch (JSONException e) {
            Logger.error("ApiAnimeTvShowsProvider<populate>: Error", e);
        }
    }

    private class ApiAnimeTvShowsFilter extends VideoFilter {
        public ApiAnimeTvShowsFilter() {
            genre.getRequestParams().add(R.string.popular, "");

            sortBy = ApiSortBy.getInstance();
        }
    }

    public static final Parcelable.Creator<ApiAnimeTvShowsListProvider> CREATOR = new Parcelable.Creator<ApiAnimeTvShowsListProvider>() {
        @Override
        public ApiAnimeTvShowsListProvider createFromParcel(Parcel source) {
            return new ApiAnimeTvShowsListProvider(source);
        }

        @Override
        public ApiAnimeTvShowsListProvider[] newArray(int size) {
            return new ApiAnimeTvShowsListProvider[size];
        }
    };
}