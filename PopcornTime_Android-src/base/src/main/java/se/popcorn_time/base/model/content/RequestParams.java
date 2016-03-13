package se.popcorn_time.base.model.content;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;

public final class RequestParams {

    private ArrayList<Integer> names = new ArrayList<>();
    private ArrayList<String> params = new ArrayList<>();
    private int position = 0;

    public RequestParams() {
    }

    public void add(@StringRes int name, String param) {
        names.add(name);
        params.add(param);
    }

    public int size() {
        return names.size();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        if (position >= 0 && position < names.size()) {
            this.position = position;
        }
    }

    public ArrayList<Integer> getNames() {
        return names;
    }

    public int getCurrentName() {
        if (position >= 0 && position < names.size()) {
            return names.get(position);
        }
        return 0;
    }

    @Nullable
    public String getCurrentParam() {
        if (position >= 0 && position < params.size()) {
            return params.get(position);
        }
        return null;
    }
}