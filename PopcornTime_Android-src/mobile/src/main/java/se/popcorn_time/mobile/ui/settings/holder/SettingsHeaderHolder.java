package se.popcorn_time.mobile.ui.settings.holder;

import android.view.ViewGroup;
import android.widget.TextView;

import dp.ws.popcorntime.R;
import se.popcorn_time.mobile.ui.recycler.RecyclerHolder;
import se.popcorn_time.mobile.ui.settings.item.SettingsHeaderItem;

public class SettingsHeaderHolder extends RecyclerHolder<SettingsHeaderItem> {

    private TextView title;

    public SettingsHeaderHolder(ViewGroup viewGroup) {
        super(viewGroup, R.layout.item_settings_header);
        title = (TextView) itemView.findViewById(R.id.settings_item_title);
    }

    @Override
    public void populate(SettingsHeaderItem item) {
        super.populate(item);
        title.setText(item.getTitle());
    }
}