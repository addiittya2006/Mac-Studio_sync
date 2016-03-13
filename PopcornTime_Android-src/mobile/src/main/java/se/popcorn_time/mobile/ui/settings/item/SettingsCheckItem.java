package se.popcorn_time.mobile.ui.settings.item;

public abstract class SettingsCheckItem extends SettingsBaseItem {

    public SettingsCheckItem() {
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
        return true;
    }
}