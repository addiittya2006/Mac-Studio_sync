package se.popcorn_time.base.providers.subtitles;

import android.os.Parcel;

import se.popcorn_time.base.providers.HttpProvider;
import se.popcorn_time.base.subtitles.Subtitles;

public abstract class SubtitlesProvider extends HttpProvider<Subtitles> {

    public SubtitlesProvider() {
    }

    protected SubtitlesProvider(Parcel source) {
        super(source);
    }

    @Override
    public Subtitles create() {
        return new Subtitles();
    }
}