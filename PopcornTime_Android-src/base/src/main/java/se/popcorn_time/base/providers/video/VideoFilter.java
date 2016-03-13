package se.popcorn_time.base.providers.video;

import se.popcorn_time.base.R;
import se.popcorn_time.base.model.content.filter.ContentFilter;
import se.popcorn_time.base.model.content.filter.FilterItem;

public class VideoFilter extends ContentFilter {

    protected FilterItem genre = new FilterItem(R.string.genre);
    protected FilterItem sortBy = new FilterItem(R.string.sort_by);
    protected FilterItem quality = new FilterItem(R.string.quality);

    public VideoFilter() {
    }

    public FilterItem getGenre() {
        return genre;
    }

    public FilterItem getSortBy() {
        return sortBy;
    }

    public FilterItem getQuality() {
        return quality;
    }
}