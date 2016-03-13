package se.popcorn_time.base.model.video.info;

import android.os.Parcel;

import java.util.ArrayList;

import se.popcorn_time.base.model.video.category.Anime;
import se.popcorn_time.base.providers.subtitles.SubtitlesProvider;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;

public class AnimeMoviesInfo extends MoviesInfo {

    public AnimeMoviesInfo() {
        super(Anime.TYPE_MOVIES);
    }

    protected AnimeMoviesInfo(Parcel parcel) {
        super(parcel);
    }

    @Override
    public ArrayList<VideoInfoProvider> getVideoInfoProviders() {
        return null;
    }

    @Override
    public ArrayList<SubtitlesProvider> getSubtitlesProviders() {
        return null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<AnimeMoviesInfo> CREATOR = new Creator<AnimeMoviesInfo>() {
        @Override
        public AnimeMoviesInfo createFromParcel(Parcel source) {
            return new AnimeMoviesInfo(source);
        }

        @Override
        public AnimeMoviesInfo[] newArray(int size) {
            return new AnimeMoviesInfo[size];
        }
    };
}