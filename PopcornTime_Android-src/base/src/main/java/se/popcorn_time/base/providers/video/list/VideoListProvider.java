package se.popcorn_time.base.providers.video.list;

import android.os.Parcel;

import java.util.ArrayList;

import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.providers.ContentProvider;
import se.popcorn_time.base.providers.video.VideoFilter;

public abstract class VideoListProvider extends ContentProvider<ArrayList<VideoInfo>> {

    protected VideoFilter videoFilter;

    public VideoListProvider() {
    }

    protected VideoListProvider(Parcel source) {
        super(source);
    }

    @Override
    public ArrayList<VideoInfo> create() {
        return new ArrayList<>();
    }

    public VideoFilter getVideoFilter() {
        return videoFilter;
    }

    public void updateParams() {
        updateParams(null);
    }

    public abstract void updateParams(String keywords);
}