package se.popcorn_time.base.model.content.category;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;

import se.popcorn_time.base.model.content.IContentProvider;
import se.popcorn_time.base.model.content.filter.ContentFilter;
import se.popcorn_time.base.model.content.subcategory.Subcategory;
import se.popcorn_time.base.providers.ContentProvider;

public abstract class Category<Data, Filter extends ContentFilter, Provider extends ContentProvider<Data>, S extends Subcategory<Data, Filter, Provider>>
        implements IContentProvider<Data, Filter, Provider> {

    private int name;
    private int subcategoryPosition = -1;
    protected ArrayList<S> subcategories = new ArrayList<>();

    public Category(@StringRes int name) {
        this.name = name;
    }

    public abstract String getType();

    public int getName() {
        return name;
    }

    public int getSubcategoryPosition() {
        return subcategoryPosition;
    }

    public void setSubcategoryPosition(int position) {
        if (position >= 0 && position < subcategories.size()) {
            this.subcategoryPosition = position;
        }
    }

    public ArrayList<S> getSubcategories() {
        return subcategories;
    }

    @Nullable
    public S getCurrentSubcategory() {
        if (subcategoryPosition >= 0 && subcategoryPosition < subcategories.size()) {
            return subcategories.get(subcategoryPosition);
        }
        return null;
    }
}