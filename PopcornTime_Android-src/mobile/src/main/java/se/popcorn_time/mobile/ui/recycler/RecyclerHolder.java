package se.popcorn_time.mobile.ui.recycler;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class RecyclerHolder<T extends RecyclerItem> extends RecyclerView.ViewHolder {

    protected T item;

    private Drawable defaultBkg;

    public RecyclerHolder(ViewGroup viewGroup, @LayoutRes int resource) {
        this(viewGroup, resource, false);
    }

    public RecyclerHolder(ViewGroup viewGroup, @LayoutRes int resource, boolean attachToRoot) {
        super(LayoutInflater.from(viewGroup.getContext()).inflate(resource, viewGroup, attachToRoot));
        defaultBkg = itemView.getBackground();
    }

    public void populate(T item) {
        this.item = item;
        Drawable background = item.background();
        if (background == null) {
            setBackground(itemView, defaultBkg);
        } else {
            setBackground(itemView, background);
        }
    }

    private void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }
}