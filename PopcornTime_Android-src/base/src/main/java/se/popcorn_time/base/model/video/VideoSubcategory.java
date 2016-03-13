package se.popcorn_time.base.model.video;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;

import se.popcorn_time.base.model.content.subcategory.Subcategory;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.providers.video.VideoFilter;
import se.popcorn_time.base.providers.video.list.VideoListProvider;

public class VideoSubcategory extends Subcategory<ArrayList<VideoInfo>, VideoFilter, VideoListProvider> {

    @Nullable
    protected VideoListProvider provider;

    public VideoSubcategory(@StringRes int name, String type) {
        super(name, type);
    }

    @Nullable
    @Override
    public VideoFilter getContentFilter() {
        return provider != null ? provider.getVideoFilter() : null;
    }

    @Nullable
    @Override
    public VideoListProvider getContentProvider() {
        return provider;
    }
}