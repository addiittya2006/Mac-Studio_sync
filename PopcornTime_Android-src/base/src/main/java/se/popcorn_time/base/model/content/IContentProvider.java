package se.popcorn_time.base.model.content;

import se.popcorn_time.base.model.content.filter.ContentFilter;
import se.popcorn_time.base.providers.ContentProvider;

public interface IContentProvider<Data, Filter extends ContentFilter, Provider extends ContentProvider<Data>> {

    Filter getContentFilter();

    Provider getContentProvider();
}