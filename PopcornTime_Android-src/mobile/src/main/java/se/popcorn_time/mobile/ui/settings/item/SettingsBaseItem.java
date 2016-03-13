package se.popcorn_time.mobile.ui.settings.item;

import se.popcorn_time.mobile.ui.recycler.RecyclerItem;
import se.popcorn_time.mobile.ui.settings.SettingsAdapter;

public abstract class SettingsBaseItem extends RecyclerItem {

    public SettingsBaseItem() {
        super(SettingsAdapter.TYPE_BASE);
    }

    public abstract boolean enabled();

    public abstract String getTitle();

    public abstract boolean isSubtitleEnabled();

    public abstract String getSubtitle();

    public abstract boolean isCheckboxEnabled();

    public abstract boolean isChecked();

    public abstract void onAction();
}