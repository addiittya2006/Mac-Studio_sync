package se.popcorn_time.base.model.video.info;

import android.os.Parcel;

import java.util.ArrayList;

import se.popcorn_time.base.model.video.category.Cinema;
import se.popcorn_time.base.providers.subtitles.ApiSubtitlesProvider;
import se.popcorn_time.base.providers.subtitles.SubtitlesProvider;
import se.popcorn_time.base.providers.subtitles.YifiSubtitlesProvider;
import se.popcorn_time.base.providers.video.info.omdb.OmdbCinemaMoviesInfoProvider;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;

public class CinemaMoviesInfo extends MoviesInfo {

    public CinemaMoviesInfo() {
        super(Cinema.TYPE_MOVIES);
    }

    private CinemaMoviesInfo(Parcel parcel) {
        super(parcel);
    }

    @Override
    public ArrayList<VideoInfoProvider> getVideoInfoProviders() {
        ArrayList<VideoInfoProvider> providers = new ArrayList<>();
        providers.add(new OmdbCinemaMoviesInfoProvider(imdb));
        return providers;
    }

    @Override
    public ArrayList<SubtitlesProvider> getSubtitlesProviders() {
        return createSubtitlesProviders(imdb);
    }

    public static ArrayList<SubtitlesProvider> createSubtitlesProviders(String imdb) {
        ArrayList<SubtitlesProvider> providers = new ArrayList<>();
        providers.add(new YifiSubtitlesProvider(imdb));
        providers.add(new ApiSubtitlesProvider(imdb));
        return providers;
    }

    public static final Creator<CinemaMoviesInfo> CREATOR = new Creator<CinemaMoviesInfo>() {
        @Override
        public CinemaMoviesInfo createFromParcel(Parcel source) {
            return new CinemaMoviesInfo(source);
        }

        @Override
        public CinemaMoviesInfo[] newArray(int size) {
            return new CinemaMoviesInfo[size];
        }
    };
}