package se.popcorn_time.mobile.ui.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

public class SingleItemTouchListener implements RecyclerView.OnItemTouchListener {

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return e.getPointerCount() > 1;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }
}