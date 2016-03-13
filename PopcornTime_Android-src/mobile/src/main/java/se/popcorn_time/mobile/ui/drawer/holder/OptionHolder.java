package se.popcorn_time.mobile.ui.drawer.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.IconTextView;
import android.widget.TextView;

import com.joanzapata.android.iconify.Iconify;

import dp.ws.popcorntime.R;
import se.popcorn_time.mobile.ui.drawer.item.OptionItem;
import se.popcorn_time.mobile.ui.recycler.RecyclerHolder;

public class OptionHolder extends RecyclerHolder<OptionItem> implements View.OnClickListener {

    private IconTextView icon;
    private TextView title;

    public OptionHolder(ViewGroup viewGroup) {
        super(viewGroup, R.layout.item_drawer);
        itemView.setOnClickListener(OptionHolder.this);
        icon = (IconTextView) itemView.findViewById(R.id.drawer_item_icon);
        title = (TextView) itemView.findViewById(R.id.drawer_item_title);
    }

    @Override
    public void populate(OptionItem item) {
        super.populate(item);
        Iconify.setIcon(icon, item.getIcon());
        title.setText(item.getTitle());
    }

    @Override
    public void onClick(View v) {
        item.onAction();
    }
}