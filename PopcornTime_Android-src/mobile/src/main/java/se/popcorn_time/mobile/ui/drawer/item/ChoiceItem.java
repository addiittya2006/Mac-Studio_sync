package se.popcorn_time.mobile.ui.drawer.item;

import com.joanzapata.android.iconify.Iconify;

import se.popcorn_time.mobile.ui.drawer.DrawerAdapter;
import se.popcorn_time.mobile.ui.recycler.RecyclerItem;

public abstract class ChoiceItem extends RecyclerItem {

    private Iconify.IconValue icon;

    public ChoiceItem(Iconify.IconValue icon) {
        super(DrawerAdapter.TYPE_CHOICE);
        this.icon = icon;
    }

    public abstract String getTitle();

    public abstract String getSubtitle();

    public abstract void onAction();

    public Iconify.IconValue getIcon() {
        return icon;
    }
}