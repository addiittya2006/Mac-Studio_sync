package se.popcorn_time.mobile.ui.recycler;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class RecyclerAdapter<T extends RecyclerItem> extends RecyclerView.Adapter<RecyclerHolder<T>> {

    private List<T> items = new ArrayList<>();

    @Override
    public void onBindViewHolder(RecyclerHolder<T> holder, int position) {
        holder.populate(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position).getType();
        }
        return super.getItemViewType(position);
    }

    public boolean add(T item) {
        if (items.add(item)) {
            item.adapter = RecyclerAdapter.this;
            item.position = items.size() - 1;
            return true;
        }
        return false;
    }

    public T remove(int position) {
        if (position >= 0 && position < items.size()) {
            T item = items.remove(position);
            if (item != null) {
                item.adapter = null;
                item.position = -1;
                return item;
            }
        }
        return null;
    }

    public boolean remove(T item) {
        if (item != null && items.remove(item)) {
            item.adapter = null;
            item.position = -1;
            return true;
        }
        return false;
    }

    public T get(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    public void clear() {
        for (T item : items) {
            if (item != null) {
                item.adapter = null;
                item.position = -1;
            }
        }
        items.clear();
    }
}