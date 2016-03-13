package se.popcorn_time.mobile.ui.settings.item;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.player.dialog.ListItemEntity;
import com.player.dialog.SingleChoiceDialog;

import java.util.List;

public abstract class SettingsChoiceItem<T> extends SettingsBaseItem {

    private SingleChoiceDialog singleChoiceDialog;
    private FragmentManager fragmentManager;
    private List<ListItemEntity<T>> listItems;
    protected ListItemEntity<T> listItem;

    public SettingsChoiceItem(FragmentManager fragmentManager, SingleChoiceDialog singleChoiceDialog) {
        super();
        this.fragmentManager = fragmentManager;
        this.singleChoiceDialog = singleChoiceDialog;
    }

    protected abstract List<ListItemEntity<T>> createItems();

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public boolean isSubtitleEnabled() {
        return true;
    }

    @Override
    public String getSubtitle() {
        return getItem() != null ? getItem().getName() : null;
    }

    @Override
    public boolean isCheckboxEnabled() {
        return false;
    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void onAction() {
        if (singleChoiceDialog == null) {
            singleChoiceDialog = new SingleChoiceDialog();
        }
        singleChoiceDialog.show(fragmentManager, getTitle(), getItems(), getItem());
    }

    @Nullable
    private List<ListItemEntity<T>> getItems() {
        if (listItems == null) {
            listItems = createItems();
        }
        return listItems;
    }

    @Nullable
    private ListItemEntity<T> getItem() {
        if (listItem == null) {
            getItems();
        }
        return listItem;
    }
}