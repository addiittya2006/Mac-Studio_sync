package se.popcorn_time.mobile.ui.base;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.database.tables.Favorites;

public abstract class VideoTypeFragment extends VideoBaseFragment {

    private boolean isFavorites;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsFavorites();
    }

    @Override
    protected void populateView(View view) {
        super.populateView(view);
        ToggleButton favorites = (ToggleButton) view.findViewById(R.id.video_favorites);
        favorites.setChecked(isFavorites);
        favorites.setOnCheckedChangeListener(favoritesListener);
    }

    private void checkIsFavorites() {
        String selection = Favorites._IMDB + "=\"" + videoInfo.imdb + "\"";
        Cursor cursor = Favorites.query(getActivity(), null, selection, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                isFavorites = true;
                Favorites.update(getActivity(), videoInfo);
            } else {
                isFavorites = false;
            }
            cursor.close();
        }
    }

    private OnCheckedChangeListener favoritesListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            isFavorites = isChecked;
            if (isChecked) {
                Favorites.insert(getActivity(), videoInfo);
            } else {
                Favorites.delete(getActivity(), videoInfo);
            }
        }
    };
}