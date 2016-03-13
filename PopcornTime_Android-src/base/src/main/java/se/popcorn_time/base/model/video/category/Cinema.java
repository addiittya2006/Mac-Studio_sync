package se.popcorn_time.base.model.video.category;

import se.popcorn_time.base.R;
import se.popcorn_time.base.model.content.category.CategoryType;
import se.popcorn_time.base.model.content.subcategory.SubcategoryType;
import se.popcorn_time.base.model.video.VideoSubcategory;
import se.popcorn_time.base.providers.video.list.api.ApiCinemaMoviesListProvider;
import se.popcorn_time.base.providers.video.list.api.ApiCinemaTvShowsListProvider;

public class Cinema extends VideoCategory {

    public static final String TYPE_MOVIES = CategoryType.CINEMA + "-" + SubcategoryType.MOVIES;
    public static final String TYPE_TV_SHOWS = CategoryType.CINEMA + "-" + SubcategoryType.TV_SHOWS;

    public Cinema() {
        super(R.string.cinema);
        subcategories.add(new Movies());
        subcategories.add(new TVShows());
        setSubcategoryPosition(0);
    }

    @Override
    public String getType() {
        return getCurrentSubcategory() != null ? CategoryType.CINEMA + "-" + getCurrentSubcategory().getType() : CategoryType.CINEMA;
    }

    private class Movies extends VideoSubcategory {
        public Movies() {
            super(R.string.movies, SubcategoryType.MOVIES);
            provider = new ApiCinemaMoviesListProvider();
        }
    }

    private class TVShows extends VideoSubcategory {
        public TVShows() {
            super(R.string.tv_shows, SubcategoryType.TV_SHOWS);
            provider = new ApiCinemaTvShowsListProvider();
        }
    }
}