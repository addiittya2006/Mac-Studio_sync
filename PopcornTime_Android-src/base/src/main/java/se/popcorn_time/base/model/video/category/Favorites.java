package se.popcorn_time.base.model.video.category;

import se.popcorn_time.base.R;
import se.popcorn_time.base.model.content.category.CategoryType;

public class Favorites extends VideoCategory {

    public Favorites() {
        super(R.string.favorites);
    }

    @Override
    public String getType() {
        return CategoryType.FAVORITES;
    }
}