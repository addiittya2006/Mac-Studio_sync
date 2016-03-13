package se.popcorn_time.mobile.ui.drawer;

import android.view.ViewGroup;

import se.popcorn_time.mobile.ui.drawer.holder.CategoryHolder;
import se.popcorn_time.mobile.ui.drawer.holder.ChoiceHolder;
import se.popcorn_time.mobile.ui.drawer.holder.DividerHolder;
import se.popcorn_time.mobile.ui.drawer.holder.OptionHolder;
import se.popcorn_time.mobile.ui.recycler.RecyclerAdapter;
import se.popcorn_time.mobile.ui.recycler.RecyclerHolder;
import se.popcorn_time.mobile.ui.recycler.RecyclerItem;

public class DrawerAdapter extends RecyclerAdapter<RecyclerItem> {

    public static final int TYPE_CATEGORY = 1;
    public static final int TYPE_DIVIDER = 2;
    public static final int TYPE_CHOICE = 3;
    public static final int TYPE_OPTION = 4;

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        if (TYPE_CATEGORY == type) {
            return new CategoryHolder(viewGroup);
        } else if (TYPE_DIVIDER == type) {
            return new DividerHolder(viewGroup);
        } else if (TYPE_CHOICE == type) {
            return new ChoiceHolder(viewGroup);
        } else if (TYPE_OPTION == type) {
            return new OptionHolder(viewGroup);
        }
        return null;
    }
}