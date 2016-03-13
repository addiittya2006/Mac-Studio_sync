package se.popcorn_time.base.model.content.subcategory;

import android.support.annotation.StringRes;

import se.popcorn_time.base.model.content.filter.ContentFilter;
import se.popcorn_time.base.model.content.IContentProvider;
import se.popcorn_time.base.providers.ContentProvider;

public abstract class Subcategory<Data, Filter extends ContentFilter, Provider extends ContentProvider<Data>>
        implements IContentProvider<Data, Filter, Provider> {

    private int name;
    private String type;

    public Subcategory(@StringRes int name, String type) {
        this.name = name;
        this.type = type;
    }

    public int getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}