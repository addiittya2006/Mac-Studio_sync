package se.popcorn_time.base.model.content.filter;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import se.popcorn_time.base.model.content.RequestParams;

public class FilterItem {

    private int name;
    private RequestParams requestParams = new RequestParams();

    public FilterItem(@StringRes int name) {
        this.name = name;
    }

    public int getName() {
        return name;
    }

    public RequestParams getRequestParams() {
        return requestParams;
    }

    @Nullable
    public String getCurrentRequestParam() {
        return requestParams.getCurrentParam();
    }

    public int getCurrentRequestName() {
        return requestParams.getCurrentName();
    }
}