package se.popcorn_time.mobile.ui.drawer.holder;

import android.view.ViewGroup;

import dp.ws.popcorntime.R;
import se.popcorn_time.mobile.ui.drawer.item.DividerItem;
import se.popcorn_time.mobile.ui.recycler.RecyclerHolder;

public class DividerHolder extends RecyclerHolder<DividerItem> {

    public DividerHolder(ViewGroup viewGroup) {
        super(viewGroup, R.layout.item_drawer_divider);
    }
}