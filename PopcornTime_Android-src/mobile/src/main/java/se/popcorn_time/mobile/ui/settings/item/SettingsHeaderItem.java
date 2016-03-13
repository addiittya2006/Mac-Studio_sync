package se.popcorn_time.mobile.ui.settings.item;

import se.popcorn_time.mobile.ui.recycler.RecyclerItem;
import se.popcorn_time.mobile.ui.settings.SettingsAdapter;

public class SettingsHeaderItem extends RecyclerItem {

    private String title;

    public SettingsHeaderItem(String title) {
        super(SettingsAdapter.TYPE_HEADER);
        this.title = title;
    }

    public SettingsHeaderItem() {
        this("");
    }

    public String getTitle() {
        return title;
    }
}