package se.popcorn_time.mobile.ui;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.joanzapata.android.iconify.Iconify;
import com.player.dialog.ListItemEntity;
import com.player.dialog.SingleChoiceDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.api.AppApi;
import se.popcorn_time.base.loader.HttpProviderLoader;
import se.popcorn_time.base.model.content.RequestParams;
import se.popcorn_time.base.model.content.category.Category;
import se.popcorn_time.base.model.content.category.CategoryType;
import se.popcorn_time.base.model.content.filter.FilterItem;
import se.popcorn_time.base.model.video.category.Anime;
import se.popcorn_time.base.model.video.category.Cinema;
import se.popcorn_time.base.model.video.category.Favorites;
import se.popcorn_time.base.model.video.category.VideoCategory;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.prefs.PopcornPrefs;
import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.prefs.SettingsPrefs;
import se.popcorn_time.base.providers.ContentProvider;
import se.popcorn_time.base.providers.video.VideoFilter;
import se.popcorn_time.base.providers.video.list.VideoListProvider;
import se.popcorn_time.base.storage.StorageUtil;
import se.popcorn_time.base.torrent.TorrentService;
import se.popcorn_time.base.torrent.client.MainClient;
import se.popcorn_time.base.updater.Updater;
import se.popcorn_time.base.utils.Logger;
import se.popcorn_time.base.vpn.VpnManager;
import se.popcorn_time.mobile.ui.base.NoFoundFragment;
import se.popcorn_time.mobile.ui.base.PopcornLoadActivity;
import se.popcorn_time.mobile.ui.drawer.Drawer;
import se.popcorn_time.mobile.ui.drawer.DrawerAdapter;
import se.popcorn_time.mobile.ui.drawer.item.CategoryItem;
import se.popcorn_time.mobile.ui.drawer.item.ChoiceItem;
import se.popcorn_time.mobile.ui.drawer.item.DividerItem;
import se.popcorn_time.mobile.ui.drawer.item.OptionItem;

public class MainActivity extends PopcornLoadActivity {

    public static final int PAGE_CINEMA_MOVIES = 0;
    public static final int PAGE_CINEMA_TV_SHOWS = 1;
    public static final int PAGE_ANIME_MOVIES = 2;
    public static final int PAGE_ANIME_TV_SHOWS = 3;
    public static final int PAGE_FAVORITES = 4;
    public static final int DEFAULT_START_PAGE = PAGE_CINEMA_MOVIES;

    final int SPLASH_SHOW_TIME = 3000;
    final int EXIT_DELAY_TIME = 2000;

    private final int CONTENT_LOADER_ID = 1001;

    private Category currentCategory;
    private CategoryItem selectedCategoryItem;

    private Drawer drawer;
    private ActionBarDrawerToggle drawerToggle;
    private MenuItem searchItem;
    private SearchView searchView;
    private GridVideoFragment videoFragment = new GridVideoFragment();
    private GridFavoritesFragment favoritesFragment = new GridFavoritesFragment();
    private SingleChoiceDialog choiceDialog = new SingleChoiceDialog();
    private Dialog splashDialog;

    private ContentProvider mLatestProvider;
    private boolean loadAfterSearchViewCollapse;
    private boolean doubleBackToExitPressedOnce = false;

    private MainClient mainClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Popcorn_Classic);
        super.onCreate(savedInstanceState);
        mainClient = new MainClient(getBaseContext());

        // Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Content
        View content = setPopcornContentView(R.layout.activity_main);
        setPopcornContentViewId(R.id.main_content);
        DrawerLayout drawerLayout = (DrawerLayout) content.findViewById(R.id.main_drawer_layout);
        RecyclerView drawerView = (RecyclerView) content.findViewById(R.id.main_drawer);
        drawer = new Drawer.Builder(MainActivity.this)
                .setLayout(drawerLayout)
                .setView(drawerView)
                .setAdapter(new DrawerAdapter())
                .build();
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, getPopcornToolbar(), 0, 0);
        drawerLayout.setDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            showSplash();
        }
        AppApi.start(MainActivity.this);
        Prefs.getPopcornPrefs().registerOnSharedPreferenceChangeListener(popcornPrefsListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initDrawer();
        drawerToggle.syncState();
        TorrentService.start(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        searchItem = menu.findItem(R.id.main_search);
        updateSearchItemVisibility();
        MenuItemCompat.setOnActionExpandListener(searchItem, searchExpandListener);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(searchListener);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mainClient.bind();
    }

    @Override
    protected void onResume() {
        super.onResume();
        optionVpnItem.notifyItemChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainClient.unbind();
    }

    @Override
    protected void onDestroy() {
        Updater.getInstance().stopUpdate(getBaseContext());
        TorrentService.stop(getBaseContext());
        super.onDestroy();
        AppApi.stop(MainActivity.this);
        Prefs.getPopcornPrefs().unregisterOnSharedPreferenceChangeListener(popcornPrefsListener);
    }

    @Override
    public void onBackPressed() {
        if (drawer.close()) {
            return;
        }
        if (collapseSearchView()) {
            return;
        }
        if (doubleBackToExitPressedOnce) {
            if (Prefs.getSettingsPrefs().get(SettingsPrefs.CLEAR_ON_EXIT, true)) {
                mainClient.removeLastOnExit();
                StorageUtil.clearCacheDir();
            }
            mainClient.exitFromApp();
            finish();
        } else {
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.exit_msg, Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, EXIT_DELAY_TIME);
    }

    @Override
    public void updateLocaleText() {
        super.updateLocaleText();
        drawer.getAdapter().notifyDataSetChanged();
        if (searchItem != null) {
            searchItem.setTitle(R.string.search);
        }
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search));
        }
    }

    @Override
    public void retryLoad() {
        if (mLatestProvider instanceof VideoListProvider) {
            restartContentLoader(mLatestProvider, videoLoaderCallbacks);
        }
    }

    private void showSplash() {
        final int orientation = getRequestedOrientation();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        splashDialog = new Dialog(MainActivity.this, R.style.FullscreenDialog);
        splashDialog.setContentView(R.layout.view_startup);
        splashDialog.setCancelable(false);
        splashDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRequestedOrientation(orientation);
                if (splashDialog != null) {
                    splashDialog.dismiss();
                    splashDialog = null;
                }
                VpnManager.getInstance().connectOnStart(MainActivity.this);
                Updater.getInstance().checkUpdate(getBaseContext());
            }
        }, SPLASH_SHOW_TIME);
    }

    private void showNoFound(int labelId) {
        NoFoundFragment noFoundFragment = new NoFoundFragment();
        noFoundFragment.setArguments(NoFoundFragment.createArguments(labelId));
        replaceFragment(noFoundFragment);
    }

    private void loadList() {
        if (currentCategory instanceof VideoCategory) {
            VideoListProvider provider = ((VideoCategory) currentCategory).getContentProvider();
            if (provider != null) {
                provider.updateParams();
            }
            restartContentLoader(provider, videoLoaderCallbacks);
        }
    }

    private void loadSearchList(String keywords) {
        if (TextUtils.isEmpty(keywords)) {
            return;
        }
        try {
            keywords = URLEncoder.encode(keywords.replaceAll("\\s+", " ").trim(), "UTF-8");
            if (currentCategory instanceof VideoCategory) {
                VideoListProvider provider = ((VideoCategory) currentCategory).getContentProvider();
                if (provider != null) {
                    provider.updateParams(keywords);
                }
                restartContentLoader(provider, videoLoaderCallbacks);
            }
            loadAfterSearchViewCollapse = true;
        } catch (UnsupportedEncodingException e) {
            Logger.error("loadSearchVideoList: " + e.getMessage());
        }
    }

    private void restartContentLoader(ContentProvider provider, LoaderManager.LoaderCallbacks callbacks) {
        if (provider == null) {
            getSupportLoaderManager().destroyLoader(CONTENT_LOADER_ID);
            showNoFound(R.string.no_result_found);
        } else {
            Bundle data = new Bundle();
            data.putParcelable("provider", provider);
            getSupportLoaderManager().restartLoader(CONTENT_LOADER_ID, data, callbacks);
            mLatestProvider = provider;
        }
    }

    private LoaderManager.LoaderCallbacks<ArrayList<VideoInfo>> videoLoaderCallbacks = new LoaderManager.LoaderCallbacks<ArrayList<VideoInfo>>() {

        private VideoListProvider provider;

        @Override
        public Loader<ArrayList<VideoInfo>> onCreateLoader(int id, Bundle args) {
            provider = args.getParcelable("provider");
            showLoading();
            return new HttpProviderLoader<>(MainActivity.this, provider);
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<VideoInfo>> loader, ArrayList<VideoInfo> data) {
            if (data != null) {
                if (data.size() > 0) {
                    videoFragment.setArguments(GridVideoFragment.createArguments(provider, data));
                    replaceFragment(videoFragment);
                } else {
                    showNoFound(R.string.no_result_found);
                }
            } else {
                showError();
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<VideoInfo>> loader) {

        }
    };

    /*
    * Search
    * */

    private void updateSearchItemVisibility() {
        if (searchItem == null) {
            return;
        }
        if (currentCategory == null || CategoryType.FAVORITES.equals(currentCategory.getType())) {
            searchItem.setVisible(false);
        } else {
            searchItem.setVisible(true);
        }
    }

    private boolean collapseSearchView() {
        if (searchItem != null && searchItem.isActionViewExpanded()) {
            searchItem.collapseActionView();
            return true;
        }
        return false;
    }

    private void collapseSearchViewWithLoadVideo(boolean loadAfterCollapse) {
        if (collapseSearchView()) {
            if (!loadAfterCollapse) {
                loadList();
            }
        } else {
            loadList();
        }
    }

    private MenuItemCompat.OnActionExpandListener searchExpandListener = new MenuItemCompat.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            getPopcornLogoView().setVisibility(View.GONE);
            drawer.close();
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            getPopcornLogoView().setVisibility(View.VISIBLE);
            if (loadAfterSearchViewCollapse) {
                loadList();
                loadAfterSearchViewCollapse = false;
            }
            return true;
        }
    };

    private SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            loadSearchList(s);
            searchView.clearFocus();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return true;
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener popcornPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PopcornPrefs.VPN_PROVIDERS.equals(key)) {
                changeCategory(selectedCategoryItem);
            }
        }
    };

    /*
    * Drawer
    * */

    private void initDrawer() {
        int page = Prefs.getSettingsPrefs().get(SettingsPrefs.START_PAGE, DEFAULT_START_PAGE);
        if (PAGE_CINEMA_MOVIES == page) {
            categoryCinemaItem.getCategory().setSubcategoryPosition(0);
            categoryCinemaItem.onSelectCategory();
        } else if (PAGE_CINEMA_TV_SHOWS == page) {
            categoryCinemaItem.getCategory().setSubcategoryPosition(1);
            categoryCinemaItem.onSelectCategory();
        } else if (PAGE_ANIME_MOVIES == page) {
            categoryAnimeItem.getCategory().setSubcategoryPosition(0);
            categoryAnimeItem.onSelectCategory();
        } else if (PAGE_ANIME_TV_SHOWS == page) {
            categoryAnimeItem.getCategory().setSubcategoryPosition(1);
            categoryAnimeItem.onSelectCategory();
        } else if (PAGE_FAVORITES == page) {
            categoryFavoritesItem.onSelectCategory();
        } else {
            Prefs.getSettingsPrefs().put(SettingsPrefs.START_PAGE, DEFAULT_START_PAGE);
            initDrawer();
        }
    }

    private void changeCategory(CategoryItem newCategoryItem) {
        this.currentCategory = newCategoryItem.getCategory();
        if (selectedCategoryItem != null) {
            selectedCategoryItem.setSelected(false);
        }
        newCategoryItem.setSelected(true);
        this.selectedCategoryItem = newCategoryItem;

        drawer.getAdapter().clear();
        drawer.getAdapter().add(categoryCinemaItem);
        drawer.getAdapter().add(categoryAnimeItem);
        drawer.getAdapter().add(categoryFavoritesItem);

        if (currentCategory instanceof VideoCategory) {
            VideoFilter videoFilter = ((VideoCategory) currentCategory).getContentFilter();
            if (videoFilter != null) {
                boolean divider = true;
                if (addDrawerFilterItem(drawer.getAdapter(), choiceGenreItem, videoFilter.getGenre(), true)) {
                    divider = false;
                }
                addDrawerFilterItem(drawer.getAdapter(), choiceSortByItem, videoFilter.getSortBy(), divider);
            }
        }

        drawer.getAdapter().add(new DividerItem());
        drawer.getAdapter().add(optionDownloadsItem);
        if (VpnManager.getInstance().isHaveProviders()) {
            drawer.getAdapter().add(optionVpnItem);
        }
        drawer.getAdapter().add(optionSettingsItem);
        drawer.getAdapter().notifyDataSetChanged();
    }

    private boolean addDrawerFilterItem(DrawerAdapter adapter, FilterChoiceItem filterChoiceItem, FilterItem filterItem, boolean divider) {
        if (filterItem != null && filterItem.getRequestParams().size() > 0) {
            if (divider) {
                adapter.add(new DividerItem());
            }
            filterChoiceItem.setFilterItem(filterItem);
            return adapter.add(filterChoiceItem);
        }
        return false;
    }

    private VideoCategoryItem categoryCinemaItem = new VideoCategoryItem(Iconify.IconValue.fa_film, new Cinema());

    private VideoCategoryItem categoryAnimeItem = new VideoCategoryItem(Iconify.IconValue.fa_paw, new Anime());

    private CategoryItem categoryFavoritesItem = new CategoryItem(Iconify.IconValue.fa_heart, new Favorites()) {
        @Override
        public void onSelectCategory() {
            getSupportLoaderManager().destroyLoader(CONTENT_LOADER_ID);
            changeCategory(this);
            updateSearchItemVisibility();
            loadAfterSearchViewCollapse = false;
            drawer.close();
            collapseSearchView();
            Cursor cursor = se.popcorn_time.base.database.tables.Favorites.query(getBaseContext(), null, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    replaceFragment(favoritesFragment);
                } else {
                    showNoFound(R.string.favorites_empty);
                }
                cursor.close();
            }
        }

        @Override
        public void onSelectSubcategory() {

        }
    };

    private FilterChoiceItem choiceGenreItem = new FilterChoiceItem(Iconify.IconValue.fa_support);

    private FilterChoiceItem choiceSortByItem = new FilterChoiceItem(Iconify.IconValue.fa_sort_amount_desc);

    private void choiceItemAction(final ChoiceItem choiceItem, final RequestParams requestParams) {
        if (requestParams != null) {
            List<ListItemEntity<Integer>> items = new ArrayList<>();
            for (int i = 0; i < requestParams.size(); i++) {
                ListItemEntity.addItemToList(items, new ListItemEntity<Integer>(i, getString(requestParams.getNames().get(i))) {
                    @Override
                    public void onItemChosen() {
                        requestParams.setPosition(getValue());
                        collapseSearchViewWithLoadVideo(loadAfterSearchViewCollapse);
                        choiceItem.notifyItemChanged();
                    }
                });
            }
            choiceDialog.show(getSupportFragmentManager(), choiceItem.getTitle(), items, items.get(requestParams.getPosition()));
        }
    }

    private OptionItem optionDownloadsItem = new OptionItem(Iconify.IconValue.fa_download, R.string.downloads) {
        @Override
        public void onAction() {
            DownloadsActivity.start(MainActivity.this);
        }
    };

    private OptionItem optionVpnItem = new OptionItem(Iconify.IconValue.fa_unlock, R.string.vpn) {
        @Override
        public Iconify.IconValue getIcon() {
            if (VpnManager.getInstance().isConnected()) {
                return Iconify.IconValue.fa_lock;
            } else {
                return super.getIcon();
            }
        }

        @Override
        public void onAction() {
            VpnActivity.start(MainActivity.this);
        }
    };

    private OptionItem optionSettingsItem = new OptionItem(Iconify.IconValue.fa_cog, R.string.settings) {
        @Override
        public void onAction() {
            SettingsActivity.start(MainActivity.this);
        }
    };

    private class VideoCategoryItem extends CategoryItem {

        public VideoCategoryItem(Iconify.IconValue icon, Category category) {
            super(icon, category);
        }

        @Override
        public void onSelectCategory() {
            changeCategory(this);
            updateSearchItemVisibility();
            collapseSearchViewWithLoadVideo(loadAfterSearchViewCollapse);
        }

        @Override
        public void onSelectSubcategory() {
            VideoFilter filter = ((VideoCategory) currentCategory).getContentFilter();
            if (filter != null) {
                if (choiceGenreItem.getAdapter() != null) {
                    choiceGenreItem.setFilterItem(filter.getGenre());
                    choiceGenreItem.notifyItemChanged();
                }
                if (choiceSortByItem.getAdapter() != null) {
                    choiceSortByItem.setFilterItem(filter.getSortBy());
                    choiceSortByItem.notifyItemChanged();
                }
            }
            collapseSearchViewWithLoadVideo(loadAfterSearchViewCollapse);
        }
    }

    private class FilterChoiceItem extends ChoiceItem {

        private FilterItem filterItem;

        public FilterChoiceItem(Iconify.IconValue icon) {
            super(icon);
        }

        public void setFilterItem(FilterItem filterItem) {
            this.filterItem = filterItem;
        }

        @Override
        public String getTitle() {
            return getString(filterItem.getName());
        }

        @Override
        public String getSubtitle() {
            return getString(filterItem.getCurrentRequestName());
        }

        @Override
        public void onAction() {
            choiceItemAction(this, filterItem.getRequestParams());
        }
    }
}