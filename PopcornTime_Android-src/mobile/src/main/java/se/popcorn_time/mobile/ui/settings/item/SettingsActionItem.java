package se.popcorn_time.mobile.ui.settings.item;

public abstract class SettingsActionItem extends SettingsBaseItem {

    public SettingsActionItem() {
        super();
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public boolean isSubtitleEnabled() {
        return true;
    }

    @Override
    public boolean isCheckboxEnabled() {
        return false;
    }

    @Override
    public boolean isChecked() {
        return false;
    }
}