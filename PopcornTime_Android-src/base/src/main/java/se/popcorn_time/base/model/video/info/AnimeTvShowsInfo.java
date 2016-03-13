package se.popcorn_time.base.model.video.info;

import android.os.Parcel;

import java.util.ArrayList;

import se.popcorn_time.base.model.video.category.Anime;
import se.popcorn_time.base.providers.subtitles.SubtitlesProvider;
import se.popcorn_time.base.providers.video.info.api.ApiAnimeTvShowsInfoProvider;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;

public class AnimeTvShowsInfo extends TvShowsInfo {

    public AnimeTvShowsInfo() {
        super(Anime.TYPE_TV_SHOWS);
    }

    protected AnimeTvShowsInfo(Parcel parcel) {
        super(parcel);
    }

    @Override
    public ArrayList<VideoInfoProvider> getVideoInfoProviders() {
        ArrayList<VideoInfoProvider> providers = new ArrayList<>();
        providers.add(new ApiAnimeTvShowsInfoProvider(imdb));
        return providers;
    }

    @Override
    public ArrayList<SubtitlesProvider> getSubtitlesProviders() {
        return null;
    }

    public static final Creator<AnimeTvShowsInfo> CREATOR = new Creator<AnimeTvShowsInfo>() {
        @Override
        public AnimeTvShowsInfo createFromParcel(Parcel source) {
            return new AnimeTvShowsInfo(source);
        }

        @Override
        public AnimeTvShowsInfo[] newArray(int size) {
            return new AnimeTvShowsInfo[size];
        }
    };
}