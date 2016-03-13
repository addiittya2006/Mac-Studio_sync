package se.popcorn_time.mobile.ui;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.database.tables.Favorites;
import se.popcorn_time.mobile.ui.adapter.FavoritesAdapter;
import se.popcorn_time.mobile.ui.base.NoFoundFragment;
import se.popcorn_time.mobile.ui.base.PopcornLoadActivity;
import se.popcorn_time.mobile.ui.locale.LocaleFragment;

public class GridFavoritesFragment extends LocaleFragment implements LoaderCallbacks<Cursor> {

    final int FAVORITES_LOADER_ID = 3409;

    private FavoritesAdapter favoritesAdapter;

    private FrameLayout contentFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritesAdapter = new FavoritesAdapter(getActivity(), null, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentFrame = new FrameLayout(getActivity().getBaseContext());
        return populateContentView(inflater, contentFrame);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getLoaderManager().initLoader(FAVORITES_LOADER_ID, null, GridFavoritesFragment.this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // change orientation
        if (contentFrame != null) {
            contentFrame.removeAllViewsInLayout();
            populateContentView(LayoutInflater.from(getActivity().getBaseContext()), contentFrame);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getLoaderManager().destroyLoader(FAVORITES_LOADER_ID);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Favorites.CONTENT_URI, null, null, null, Favorites._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            favoritesAdapter.swapCursor(cursor);
        } else {
            if (isAdded()) {
                NoFoundFragment noFoundFragment = new NoFoundFragment();
                noFoundFragment.setArguments(NoFoundFragment.createArguments(R.string.favorites_empty));
                ((PopcornLoadActivity) getActivity()).replaceFragment(noFoundFragment);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        favoritesAdapter.swapCursor(null);
    }

    private View populateContentView(LayoutInflater inflater, ViewGroup content) {
        GridView videoGrid = (GridView) inflater.inflate(R.layout.fragment_grid_video, content, false);
        videoGrid.setAdapter(favoritesAdapter);
        content.addView(videoGrid);
        return content;
    }
}