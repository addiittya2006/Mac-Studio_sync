package se.popcorn_time.mobile.ui.drawer.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.IconTextView;
import android.widget.TextView;

import com.joanzapata.android.iconify.Iconify;

import dp.ws.popcorntime.R;
import se.popcorn_time.mobile.ui.drawer.item.ChoiceItem;
import se.popcorn_time.mobile.ui.recycler.RecyclerHolder;

public class ChoiceHolder extends RecyclerHolder<ChoiceItem> implements View.OnClickListener {

    private IconTextView icon;
    private TextView title;
    private TextView subtitle;

    public ChoiceHolder(ViewGroup viewGroup) {
        super(viewGroup, R.layout.item_drawer_choice);
        itemView.setOnClickListener(ChoiceHolder.this);
        icon = (IconTextView) itemView.findViewById(R.id.drawer_item_icon);
        title = (TextView) itemView.findViewById(R.id.drawer_item_title);
        subtitle = (TextView) itemView.findViewById(R.id.drawer_item_subtitle);
    }

    @Override
    public void populate(ChoiceItem item) {
        super.populate(item);
        Iconify.setIcon(icon, item.getIcon());
        title.setText(item.getTitle());
        subtitle.setText(item.getSubtitle());
    }

    @Override
    public void onClick(View v) {
        item.onAction();
    }
}