package se.popcorn_time.mobile.ui.drawer;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import se.popcorn_time.mobile.ui.recycler.SingleItemTouchListener;

public class Drawer {
    private DrawerLayout layout;
    private RecyclerView view;
    private DrawerAdapter adapter;

    protected Drawer() {

    }

    protected void setLayout(DrawerLayout layout) {
        this.layout = layout;
    }

    protected void setView(RecyclerView view) {
        this.view = view;
    }

    protected void setAdapter(DrawerAdapter adapter) {
        this.adapter = adapter;
    }

    public DrawerAdapter getAdapter() {
        return adapter;
    }

    public boolean open() {
        if (layout != null && !isShown()) {
            layout.openDrawer(view);
            return true;
        }
        return false;
    }

    public boolean close() {
        if (layout != null && isShown()) {
            layout.closeDrawer(view);
            return true;
        }
        return false;
    }

    public boolean isShown() {
        return layout != null && layout.isDrawerOpen(view);
    }

    /*
    * Builder
    * */

    public static class Builder {
        private final BuilderParams P;

        public Builder(Activity activity) {
            P = new BuilderParams(activity);
        }

        public Builder setLayout(DrawerLayout layout) {
            P.layout = layout;
            return this;
        }

        public Builder setView(RecyclerView view) {
            P.view = view;
            return this;
        }

        public Builder setAdapter(DrawerAdapter adapter) {
            P.adapter = adapter;
            return this;
        }

        public Drawer build() {
            if (P.activity == null) {
                throw new NullPointerException("Activity is null");
            }
            if (P.view == null) {
                throw new NullPointerException("RecyclerView is null");
            }
            Drawer drawer = new Drawer();
            drawer.setView(P.view);
            drawer.setLayout(P.layout);
            P.view.setLayoutManager(new LinearLayoutManager(P.activity));
            P.view.addOnItemTouchListener(new SingleItemTouchListener());
            if (P.adapter != null) {
                P.view.setAdapter(P.adapter);
                drawer.setAdapter(P.adapter);
            }
            return drawer;
        }
    }

    /*
    * Params
    * */

    protected static class BuilderParams {
        public Activity activity;
        public DrawerLayout layout;
        public RecyclerView view;
        public DrawerAdapter adapter;

        public BuilderParams(Activity activity) {
            this.activity = activity;
        }
    }
}