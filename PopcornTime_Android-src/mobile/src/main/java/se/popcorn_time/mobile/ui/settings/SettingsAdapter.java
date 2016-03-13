package se.popcorn_time.mobile.ui.settings;

import android.view.ViewGroup;

import se.popcorn_time.mobile.ui.recycler.RecyclerAdapter;
import se.popcorn_time.mobile.ui.recycler.RecyclerHolder;
import se.popcorn_time.mobile.ui.recycler.RecyclerItem;
import se.popcorn_time.mobile.ui.settings.holder.SettingsBaseHolder;
import se.popcorn_time.mobile.ui.settings.holder.SettingsHeaderHolder;

public class SettingsAdapter extends RecyclerAdapter<RecyclerItem> {

    public static final int TYPE_HEADER = 1;
    public static final int TYPE_BASE = 2;

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_HEADER == viewType) {
            return new SettingsHeaderHolder(parent);
        } else if (TYPE_BASE == viewType) {
            return new SettingsBaseHolder(parent);
        }
        return null;
    }
}