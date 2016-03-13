package se.popcorn_time.base.providers.video.list.api;

import se.popcorn_time.base.R;
import se.popcorn_time.base.model.content.filter.FilterItem;

public class ApiSortBy extends FilterItem {

    private static final ApiSortBy INSTANCE = new ApiSortBy();

    private ApiSortBy() {
        super(R.string.sort_by);
        getRequestParams().add(R.string.popularity, "seeds");
        getRequestParams().add(R.string.date_added, "dateadded");
        getRequestParams().add(R.string.year, "year");
    }

    public static ApiSortBy getInstance() {
        return INSTANCE;
    }
}