package se.popcorn_time.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.player.dialog.ListItemEntity;
import com.player.dialog.SingleChoiceDialog;

import org.videolan.vlc.util.VLCOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.prefs.PopcornPrefs;
import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.prefs.SettingsPrefs;
import se.popcorn_time.base.receiver.ConnectivityReceiver;
import se.popcorn_time.base.storage.StorageMount;
import se.popcorn_time.base.storage.StorageUtil;
import se.popcorn_time.base.subtitles.SubtitlesFontColor;
import se.popcorn_time.base.subtitles.SubtitlesFontSize;
import se.popcorn_time.base.subtitles.SubtitlesLanguage;
import se.popcorn_time.base.torrent.TorrentService;
import se.popcorn_time.base.torrent.client.BaseClient;
import se.popcorn_time.base.utils.InterfaceUtil;
import se.popcorn_time.base.utils.Logger;
import se.popcorn_time.base.vpn.VpnManager;
import se.popcorn_time.mobile.ui.base.PopcornBaseActivity;
import se.popcorn_time.mobile.ui.dialog.StorageDialog;
import se.popcorn_time.mobile.ui.recycler.SingleItemTouchListener;
import se.popcorn_time.mobile.ui.settings.SettingsAdapter;
import se.popcorn_time.mobile.ui.settings.item.SettingsActionItem;
import se.popcorn_time.mobile.ui.settings.item.SettingsCheckItem;
import se.popcorn_time.mobile.ui.settings.item.SettingsChoiceItem;
import se.popcorn_time.mobile.ui.settings.item.SettingsHeaderItem;

public class SettingsActivity extends PopcornBaseActivity {

    final int[] PAGES = new int[]{
            MainActivity.PAGE_CINEMA_MOVIES,
            MainActivity.PAGE_CINEMA_TV_SHOWS,
            MainActivity.PAGE_ANIME_MOVIES,
            MainActivity.PAGE_ANIME_TV_SHOWS,
            MainActivity.PAGE_FAVORITES,
    };

    final int[] HW_ACCELERATIONS = new int[]{
            VLCOptions.HW_ACCELERATION_AUTOMATIC,
            VLCOptions.HW_ACCELERATION_DISABLED,
            VLCOptions.HW_ACCELERATION_DECODING,
            VLCOptions.HW_ACCELERATION_FULL
    };

    final int[] SPEEDS = new int[]{
            0,
            TorrentService.MINIMUM_SPEED,
            2 * TorrentService.MINIMUM_SPEED,
            5 * TorrentService.MINIMUM_SPEED,
            10 * TorrentService.MINIMUM_SPEED,
            20 * TorrentService.MINIMUM_SPEED,
            50 * TorrentService.MINIMUM_SPEED,
            100 * TorrentService.MINIMUM_SPEED
    };

    private String[] themes;
    private String[] pages = new String[5];
    private String[] hwAccelerations;
    private String[] subtitlesFontSizes;
    private String[] subtitlesFontColors;
    private String[] speeds;

    private final int REQUEST_DIRECTORY = 3457;

    private final String APP_SITE = "http://popcorn-time.se/";
    private final String APP_FORUM = "http://forum.popcorn-time.se/";

    private SettingsAdapter adapter;
    private BaseClient baseClient;
    private SingleChoiceDialog singleChoiceDialog = new SingleChoiceDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Popcorn_Classic);
        super.onCreate(savedInstanceState);
        baseClient = new BaseClient(SettingsActivity.this);

        speeds = new String[SPEEDS.length];
        for (int i = 1; i < SPEEDS.length; i++) {
            speeds[i] = (SPEEDS[i] / 1000) + " KB/s";
        }

        // Toolbar
        getPopcornLogoView().setVisibility(View.GONE);
        getPopcornTitle().setVisibility(View.VISIBLE);

        // Content
        adapter = new SettingsAdapter();
        RecyclerView view = (RecyclerView) setPopcornContentView(R.layout.activity_settings);
        view.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        view.addOnItemTouchListener(new SingleItemTouchListener());
        view.setAdapter(adapter);
        initAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
        baseClient.bind();
    }

    @Override
    protected void onStop() {
        super.onStop();
        baseClient.unbind();
    }

    @Override
    public void updateLocaleText() {
        super.updateLocaleText();
        SubtitlesLanguage.setWithoutSubtitlesText(getString(R.string.without_subtitle));
        getPopcornTitle().setText(R.string.settings);

        themes = getResources().getStringArray(R.array.themes);

        pages[0] = getString(R.string.cinema) + " - " + getString(R.string.movies);
        pages[1] = getString(R.string.cinema) + " - " + getString(R.string.tv_shows);
        pages[2] = getString(R.string.anime) + " - " + getString(R.string.movies);
        pages[3] = getString(R.string.anime) + " - " + getString(R.string.tv_shows);
        pages[4] = getString(R.string.favorites);

        hwAccelerations = getResources().getStringArray(R.array.accelerations);
        subtitlesFontSizes = getResources().getStringArray(R.array.font_size_names);
        subtitlesFontColors = getResources().getStringArray(R.array.font_color_names);

        speeds[0] = getString(R.string.unlimited);

        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (REQUEST_DIRECTORY == requestCode) {
                StorageUtil.setRootDir(new File(data.getStringExtra(FolderChooserActivity.SELECTED_DIR)));
                cacheFolderItem.notifyItemChanged();
            }
        }
    }

    private void initAdapter() {
        adapter.add(new SettingsHeaderItem() {
            @Override
            public String getTitle() {
                return getString(R.string.interface_);
            }
        });
        adapter.add(languageItem);
        adapter.add(themeItem);
        adapter.add(startPageItem);
        adapter.add(new SettingsHeaderItem() {
            @Override
            public String getTitle() {
                return getString(R.string.player);
            }
        });
        adapter.add(hwAccelerationItem);
        adapter.add(new SettingsHeaderItem() {
            @Override
            public String getTitle() {
                return getString(R.string.subtitles);
            }
        });
        adapter.add(subtitleLanguageItem);
        adapter.add(subtitleFontSizeItem);
        adapter.add(subtitleFontColorItem);
        adapter.add(new SettingsHeaderItem() {
            @Override
            public String getTitle() {
                return getString(R.string.downloads);
            }
        });
        if (VpnManager.getInstance().isHaveProviders()) {
            adapter.add(checkVpnConnection);
        }
        adapter.add(onlyWifiConnectionItem);
        adapter.add(maximumDownloadSpeedItem);
        adapter.add(maximumUploadSpeedItem);
        adapter.add(cacheFolderItem);
        adapter.add(clearCacheFolderItem);
        adapter.add(new SettingsHeaderItem() {
            @Override
            public String getTitle() {
                return getString(R.string.about);
            }
        });
        adapter.add(visitSiteItem);
        adapter.add(visitForumItem);
        adapter.add(versionItem);
    }

    /*
    * Interface
    * */

    private SettingsChoiceItem languageItem = new SettingsChoiceItem<String>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.language);
        }

        @Override
        protected List<ListItemEntity<String>> createItems() {
            String locale = Prefs.getPopcornPrefs().get(PopcornPrefs.APP_LOCALE, InterfaceUtil.DEFAULT_INTERFACE_LOCALE);
            List<ListItemEntity<String>> list = new ArrayList<>();
            for (int i = 0; i < InterfaceUtil.getInterfaceNative().length; i++) {
                ListItemEntity<String> _listItem = new ListItemEntity<String>(InterfaceUtil.getInterfaceLocale()[i], InterfaceUtil.getInterfaceNative()[i]) {
                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        InterfaceUtil.changeAppLocale(getValue());
                        mLocaleHelper.checkLanguage();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && locale.equals(_listItem.getValue())) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    private SettingsChoiceItem themeItem = new SettingsChoiceItem<String>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.theme);
        }

        @Override
        protected List<ListItemEntity<String>> createItems() {
            List<ListItemEntity<String>> list = new ArrayList<>();
            for (int i = 0; i < themes.length; i++) {
                ListItemEntity<String> _listItem = new ListItemEntity<String>(Integer.toString(i)) {
                    @Override
                    public String getName() {
                        return themes[getPosition()];
                    }

                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        notifyItemChanged();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && i == 0) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    private SettingsChoiceItem startPageItem = new SettingsChoiceItem<Integer>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.start_page);
        }

        @Override
        protected List<ListItemEntity<Integer>> createItems() {
            int start_page = Prefs.getSettingsPrefs().get(SettingsPrefs.START_PAGE, MainActivity.DEFAULT_START_PAGE);
            List<ListItemEntity<Integer>> list = new ArrayList<>();
            for (int page : PAGES) {
                ListItemEntity<Integer> _listItem = new ListItemEntity<Integer>(page) {
                    @Override
                    public String getName() {
                        return pages[getPosition()];
                    }

                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        Prefs.getSettingsPrefs().put(SettingsPrefs.START_PAGE, getValue());
                        notifyItemChanged();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && start_page == _listItem.getValue()) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    /*
    * Player
    * */

    private SettingsChoiceItem hwAccelerationItem = new SettingsChoiceItem<Integer>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.hardware_acceleration);
        }

        @Override
        protected List<ListItemEntity<Integer>> createItems() {
            int hw_acc = Prefs.getSettingsPrefs().get(SettingsPrefs.HARDWARE_ACCELERATION, VLCOptions.HW_ACCELERATION_AUTOMATIC);
            List<ListItemEntity<Integer>> list = new ArrayList<>();
            for (int acceleration : HW_ACCELERATIONS) {
                ListItemEntity<Integer> _listItem = new ListItemEntity<Integer>(acceleration) {
                    @Override
                    public String getName() {
                        return hwAccelerations[getPosition()];
                    }

                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        Prefs.getSettingsPrefs().put(SettingsPrefs.HARDWARE_ACCELERATION, getValue());
                        notifyItemChanged();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && hw_acc == _listItem.getValue()) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    /*
    * Subtitles
    * */

    private SettingsChoiceItem subtitleLanguageItem = new SettingsChoiceItem<String>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.default_subtitle);
        }

        @Override
        protected List<ListItemEntity<String>> createItems() {
            String subLang = Prefs.getSettingsPrefs().get(SettingsPrefs.SUBTITLE_LANGUAGE, SubtitlesLanguage.DEFAULT_SUBTITLE_LANGUAGE);
            List<ListItemEntity<String>> list = new ArrayList<>();
            for (int i = 0; i < SubtitlesLanguage.getSubtitlesName().length; i++) {
                ListItemEntity<String> _listItem = new ListItemEntity<String>(SubtitlesLanguage.getSubtitlesName()[i]) {
                    @Override
                    public String getName() {
                        return SubtitlesLanguage.getSubtitlesNative()[getPosition()];
                    }

                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        Prefs.getSettingsPrefs().put(SettingsPrefs.SUBTITLE_LANGUAGE, getValue());
                        notifyItemChanged();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && subLang.equals(_listItem.getValue())) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    private SettingsChoiceItem subtitleFontSizeItem = new SettingsChoiceItem<Float>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.font_size);
        }

        @Override
        protected List<ListItemEntity<Float>> createItems() {
            float size = Prefs.getSettingsPrefs().get(SettingsPrefs.SUBTITLE_FONT_SIZE, SubtitlesFontSize.DEFAULT_SIZE);
            List<ListItemEntity<Float>> list = new ArrayList<>();
            for (int i = 0; i < SubtitlesFontSize.SIZES.length; i++) {
                ListItemEntity<Float> _listItem = new ListItemEntity<Float>(SubtitlesFontSize.SIZES[i]) {
                    @Override
                    public String getName() {
                        return subtitlesFontSizes[getPosition()];
                    }

                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        Prefs.getSettingsPrefs().put(SettingsPrefs.SUBTITLE_FONT_SIZE, getValue());
                        notifyItemChanged();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && size == _listItem.getValue()) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    private SettingsChoiceItem subtitleFontColorItem = new SettingsChoiceItem<String>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.font_color);
        }

        @Override
        protected List<ListItemEntity<String>> createItems() {
            String color = Prefs.getSettingsPrefs().get(SettingsPrefs.SUBTITLE_FONT_COLOR, SubtitlesFontColor.DEFAULT_COLOR);
            List<ListItemEntity<String>> list = new ArrayList<>();
            for (int i = 0; i < SubtitlesFontColor.COLORS.length; i++) {
                ListItemEntity<String> _listItem = new ListItemEntity<String>(SubtitlesFontColor.COLORS[i]) {
                    @Override
                    public String getName() {
                        return subtitlesFontColors[getPosition()];
                    }

                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        Prefs.getSettingsPrefs().put(SettingsPrefs.SUBTITLE_FONT_COLOR, getValue());
                        notifyItemChanged();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && color.equals(_listItem.getValue())) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    /*
    * Downloads
    * */

    private SettingsCheckItem checkVpnConnection = new SettingsCheckItem() {
        @Override
        public String getTitle() {
            return getString(R.string.check_connection);
        }

        @Override
        public String getSubtitle() {
            return getString(R.string.check_connection_description);
        }

        @Override
        public boolean isChecked() {
            return Prefs.getPopcornPrefs().get(PopcornPrefs.CHECK_VPN_CONNECTION, PopcornPrefs.DEFAULT_CHECK_VPN_CONNECTION);
        }

        @Override
        public void onAction() {
            Prefs.getPopcornPrefs().put(PopcornPrefs.CHECK_VPN_CONNECTION, !isChecked());
            notifyItemChanged();
        }
    };

    private SettingsCheckItem onlyWifiConnectionItem = new SettingsCheckItem() {
        @Override
        public String getTitle() {
            return getString(R.string.use_only_wifi);
        }

        @Override
        public String getSubtitle() {
            if (isChecked()) {
                return getString(R.string.enabled);
            }
            return getString(R.string.disabled);
        }

        @Override
        public boolean isChecked() {
            return Prefs.getSettingsPrefs().get(SettingsPrefs.ONLY_WIFI_CONNECTION, ConnectivityReceiver.DEFAULT_ONLY_WIFI_CONNECTION);
        }

        @Override
        public void onAction() {
            Prefs.getSettingsPrefs().put(SettingsPrefs.ONLY_WIFI_CONNECTION, !isChecked());
            notifyItemChanged();
        }
    };

    private SettingsChoiceItem maximumDownloadSpeedItem = new SettingsChoiceItem<Integer>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.maximum_download_speed);
        }

        @Override
        protected List<ListItemEntity<Integer>> createItems() {
            int _speed = Prefs.getSettingsPrefs().get(SettingsPrefs.MAXIMUM_DOWNLOAD_SPEED, TorrentService.DEFAULT_DOWNLOAD_SPEED);
            List<ListItemEntity<Integer>> list = new ArrayList<>();
            for (int speed : SPEEDS) {
                ListItemEntity<Integer> _listItem = new ListItemEntity<Integer>(speed) {
                    @Override
                    public String getName() {
                        return speeds[getPosition()];
                    }

                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        Prefs.getSettingsPrefs().put(SettingsPrefs.MAXIMUM_DOWNLOAD_SPEED, getValue());
                        baseClient.setMaximumDownloadSpeed(getValue());
                        notifyItemChanged();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && _speed == _listItem.getValue()) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    private SettingsChoiceItem maximumUploadSpeedItem = new SettingsChoiceItem<Integer>(getSupportFragmentManager(), singleChoiceDialog) {
        @Override
        public String getTitle() {
            return getString(R.string.maximum_upload_speed);
        }

        @Override
        protected List<ListItemEntity<Integer>> createItems() {
            int _speed = Prefs.getSettingsPrefs().get(SettingsPrefs.MAXIMUM_UPLOAD_SPEED, TorrentService.DEFAULT_UPLOAD_SPEED);
            List<ListItemEntity<Integer>> list = new ArrayList<>();
            for (int speed : SPEEDS) {
                ListItemEntity<Integer> _listItem = new ListItemEntity<Integer>(speed) {
                    @Override
                    public String getName() {
                        return speeds[getPosition()];
                    }

                    @Override
                    public void onItemChosen() {
                        listItem = this;
                        Prefs.getSettingsPrefs().put(SettingsPrefs.MAXIMUM_UPLOAD_SPEED, getValue());
                        baseClient.setMaximumUploadSpeed(getValue());
                        notifyItemChanged();
                    }
                };
                ListItemEntity.addItemToList(list, _listItem);
                if (listItem == null && _speed == _listItem.getValue()) {
                    listItem = _listItem;
                }
            }
            return list;
        }
    };

    private SettingsActionItem cacheFolderItem = new SettingsActionItem() {
        @Override
        public String getTitle() {
            return getString(R.string.cache_folder);
        }

        @Override
        public String getSubtitle() {
            if (TextUtils.isEmpty(StorageUtil.getRootDirPath())) {
                return getResources().getString(R.string.cache_folder_not_selected);
            }
            return StorageUtil.getRootDirPath();
        }

        @Override
        public void onAction() {
            List<ListItemEntity<StorageMount>> listItemEntities = new ArrayList<>();
            for (StorageMount storage : StorageUtil.getStorageMounts()) {
                ListItemEntity<StorageMount> listItemEntity = new ListItemEntity<StorageMount>(storage) {
                    @Override
                    public void onItemChosen() {
                        if (getValue().primary) {
                            FolderChooserActivity.startForResult(SettingsActivity.this, getValue().dir, REQUEST_DIRECTORY);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                StorageUtil.setRootDir(getValue().dir);
                                notifyItemChanged();
                                // TODO: notify about delete video after remove app
                            } else {
                                FolderChooserActivity.startForResult(SettingsActivity.this, getValue().dir, REQUEST_DIRECTORY);
                            }
                        }
                    }
                };
                ListItemEntity.addItemToList(listItemEntities, listItemEntity);
            }
            new StorageDialog().show(getSupportFragmentManager(), getString(R.string.cache_folder), listItemEntities, StorageUtil.getRootDir());
        }
    };

    private SettingsCheckItem clearCacheFolderItem = new SettingsCheckItem() {
        @Override
        public String getTitle() {
            return getString(R.string.clear_cache_folder_on_exit);
        }

        @Override
        public String getSubtitle() {
            if (isChecked()) {
                return getString(R.string.enabled);
            }
            return getString(R.string.disabled);
        }

        @Override
        public boolean isChecked() {
            return Prefs.getSettingsPrefs().get(SettingsPrefs.CLEAR_ON_EXIT, StorageUtil.DEFAULT_CLEAR);
        }

        @Override
        public void onAction() {
            Prefs.getSettingsPrefs().put(SettingsPrefs.CLEAR_ON_EXIT, !isChecked());
            notifyItemChanged();
        }
    };

    /*
    * About
    * */

    private SettingsActionItem visitSiteItem = new SettingsActionItem() {
        @Override
        public String getTitle() {
            return getString(R.string.visit_site);
        }

        @Override
        public String getSubtitle() {
            return APP_SITE;
        }

        @Override
        public void onAction() {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_SITE));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SettingsActivity.this.startActivity(intent);
        }
    };

    private SettingsActionItem visitForumItem = new SettingsActionItem() {
        @Override
        public String getTitle() {
            return getString(R.string.visit_forum);
        }

        @Override
        public String getSubtitle() {
            return APP_FORUM;
        }

        @Override
        public void onAction() {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(APP_FORUM));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SettingsActivity.this.startActivity(intent);
        }
    };

    private SettingsActionItem versionItem = new SettingsActionItem() {

        @Override
        public String getTitle() {
            return getString(R.string.version);
        }

        @Override
        public String getSubtitle() {
            try {
                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                return pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                Logger.error("SettingsActivity<versionItem>: Error", e);
            }
            return "";
        }

        @Override
        public void onAction() {

        }
    };

    /*
    * Static
    * */

    public static void start(Context context) {
        context.startActivity(new Intent(context, SettingsActivity.class));
    }
}