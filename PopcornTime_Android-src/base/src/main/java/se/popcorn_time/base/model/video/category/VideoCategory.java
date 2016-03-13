package se.popcorn_time.base.model.video.category;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;

import se.popcorn_time.base.model.content.category.Category;
import se.popcorn_time.base.model.video.VideoSubcategory;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.providers.video.VideoFilter;
import se.popcorn_time.base.providers.video.list.VideoListProvider;

public class VideoCategory extends Category<ArrayList<VideoInfo>, VideoFilter, VideoListProvider, VideoSubcategory> {

    public VideoCategory(@StringRes int name) {
        super(name);
    }

    @Override
    public String getType() {
        return null;
    }

    @Nullable
    @Override
    public VideoFilter getContentFilter() {
        return getCurrentSubcategory() != null ? getCurrentSubcategory().getContentFilter() : null;
    }

    @Nullable
    @Override
    public VideoListProvider getContentProvider() {
        return getCurrentSubcategory() != null ? getCurrentSubcategory().getContentProvider() : null;
    }
}