package se.popcorn_time.base.model.video.info;

import android.os.Parcel;

import java.util.ArrayList;

import se.popcorn_time.base.model.video.category.Cinema;
import se.popcorn_time.base.providers.subtitles.ApiSubtitlesProvider;
import se.popcorn_time.base.providers.subtitles.SubtitlesProvider;
import se.popcorn_time.base.providers.video.info.api.ApiCinemaTvShowsInfoProvider;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;

public class CinemaTvShowsInfo extends TvShowsInfo {

    public CinemaTvShowsInfo() {
        super(Cinema.TYPE_TV_SHOWS);
    }

    private CinemaTvShowsInfo(Parcel parcel) {
        super(parcel);
    }

    @Override
    public ArrayList<VideoInfoProvider> getVideoInfoProviders() {
        ArrayList<VideoInfoProvider> providers = new ArrayList<>();
        providers.add(new ApiCinemaTvShowsInfoProvider(imdb));
        return providers;
    }

    @Override
    public ArrayList<SubtitlesProvider> getSubtitlesProviders() {
        Season season = getCurrentSeason();
        Episode episode = getCurrentEpisode();
        if (season != null && episode != null) {
            return createSubtitlesProviders(imdb, season.getNumber(), episode.getNumber());
        }
        return null;
    }

    public static ArrayList<SubtitlesProvider> createSubtitlesProviders(String imdb, int season, int episode) {
        ArrayList<SubtitlesProvider> providers = new ArrayList<>();
        providers.add(new ApiSubtitlesProvider(imdb, season, episode));
        return providers;
    }

    public static final Creator<CinemaTvShowsInfo> CREATOR = new Creator<CinemaTvShowsInfo>() {
        @Override
        public CinemaTvShowsInfo createFromParcel(Parcel source) {
            return new CinemaTvShowsInfo(source);
        }

        @Override
        public CinemaTvShowsInfo[] newArray(int size) {
            return new CinemaTvShowsInfo[size];
        }
    };
}