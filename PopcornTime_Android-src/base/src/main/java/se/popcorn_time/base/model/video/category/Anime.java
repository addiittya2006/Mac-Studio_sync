package se.popcorn_time.base.model.video.category;

import se.popcorn_time.base.R;
import se.popcorn_time.base.model.content.category.CategoryType;
import se.popcorn_time.base.model.content.subcategory.SubcategoryType;
import se.popcorn_time.base.model.video.VideoSubcategory;
import se.popcorn_time.base.providers.video.list.api.ApiAnimeMoviesListProvider;
import se.popcorn_time.base.providers.video.list.api.ApiAnimeTvShowsListProvider;

public class Anime extends VideoCategory {

    public static final String TYPE_MOVIES = CategoryType.ANIME + "-" + SubcategoryType.MOVIES;
    public static final String TYPE_TV_SHOWS = CategoryType.ANIME + "-" + SubcategoryType.TV_SHOWS;

    public Anime() {
        super(R.string.anime);
        subcategories.add(new Movies());
        subcategories.add(new TVShows());
        setSubcategoryPosition(0);
    }

    @Override
    public String getType() {
        return getCurrentSubcategory() != null ? CategoryType.ANIME + "-" + getCurrentSubcategory().getType() : CategoryType.ANIME;
    }

    private class Movies extends VideoSubcategory {
        public Movies() {
            super(R.string.movies, SubcategoryType.MOVIES);
            provider = new ApiAnimeMoviesListProvider();
        }
    }

    private class TVShows extends VideoSubcategory {
        public TVShows() {
            super(R.string.tv_shows, SubcategoryType.TV_SHOWS);
            provider = new ApiAnimeTvShowsListProvider();
        }
    }
}