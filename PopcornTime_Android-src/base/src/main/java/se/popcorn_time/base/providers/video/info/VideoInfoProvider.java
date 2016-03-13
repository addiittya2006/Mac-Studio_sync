package se.popcorn_time.base.providers.video.info;

import android.os.Parcel;

import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.providers.HttpProvider;

public abstract class VideoInfoProvider<Info extends VideoInfo> extends HttpProvider<Info> {

    public VideoInfoProvider() {
    }

    protected VideoInfoProvider(Parcel source) {
        super(source);
    }
}