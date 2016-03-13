package se.popcorn_time.mobile.ui.drawer.item;

import android.support.annotation.StringRes;

import com.joanzapata.android.iconify.Iconify;

import se.popcorn_time.mobile.ui.drawer.DrawerAdapter;
import se.popcorn_time.mobile.ui.recycler.RecyclerItem;

public abstract class OptionItem extends RecyclerItem {

    private Iconify.IconValue icon;
    private int title;

    public OptionItem(Iconify.IconValue icon, @StringRes int title) {
        super(DrawerAdapter.TYPE_OPTION);
        this.icon = icon;
        this.title = title;
    }

    public abstract void onAction();

    public Iconify.IconValue getIcon() {
        return icon;
    }

    public int getTitle() {
        return title;
    }
}