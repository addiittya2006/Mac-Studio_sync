package se.popcorn_time.mobile.ui.settings.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import dp.ws.popcorntime.R;
import se.popcorn_time.mobile.ui.recycler.RecyclerHolder;
import se.popcorn_time.mobile.ui.settings.item.SettingsBaseItem;

public class SettingsBaseHolder extends RecyclerHolder<SettingsBaseItem> implements View.OnClickListener {

    private TextView title;
    private TextView subtitle;
    private CheckBox checkbox;

    public SettingsBaseHolder(ViewGroup viewGroup) {
        super(viewGroup, R.layout.item_settings_base);
        itemView.setOnClickListener(SettingsBaseHolder.this);
        title = (TextView) itemView.findViewById(R.id.settings_item_title);
        subtitle = (TextView) itemView.findViewById(R.id.settings_item_subtitle);
        checkbox = (CheckBox) itemView.findViewById(R.id.settings_item_checkbox);
    }

    @Override
    public void populate(SettingsBaseItem item) {
        super.populate(item);
        itemView.setEnabled(item.enabled());
        title.setText(item.getTitle());
        if (item.isSubtitleEnabled()) {
            subtitle.setVisibility(View.VISIBLE);
            subtitle.setText(item.getSubtitle());
        } else {
            subtitle.setVisibility(View.GONE);
        }
        if (item.isCheckboxEnabled()) {
            checkbox.setVisibility(View.VISIBLE);
            checkbox.setChecked(item.isChecked());
        } else {
            checkbox.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        item.onAction();
    }
}