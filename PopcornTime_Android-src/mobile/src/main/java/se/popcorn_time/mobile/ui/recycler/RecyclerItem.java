package se.popcorn_time.mobile.ui.recycler;

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public abstract class RecyclerItem {

    private int type;
    protected int position;
    protected RecyclerAdapter<? extends RecyclerItem> adapter;

    public RecyclerItem(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public RecyclerAdapter<? extends RecyclerItem> getAdapter() {
        return adapter;
    }

    public final void notifyItemChanged() {
        if (adapter != null) {
            adapter.notifyItemChanged(position);
        }
    }

    @Nullable
    protected Drawable background() {
        return null;
    }
}