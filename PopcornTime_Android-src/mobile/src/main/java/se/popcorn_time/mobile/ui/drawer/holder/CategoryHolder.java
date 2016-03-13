package se.popcorn_time.mobile.ui.drawer.holder;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.IconTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andexert.expandablelayout.library.ExpandableLayout;
import com.joanzapata.android.iconify.Iconify;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.model.content.subcategory.Subcategory;
import se.popcorn_time.mobile.ui.drawer.item.CategoryItem;
import se.popcorn_time.mobile.ui.recycler.RecyclerHolder;

public class CategoryHolder extends RecyclerHolder<CategoryItem> implements View.OnClickListener {

    private ExpandableLayout layout;
    private IconTextView icon;
    private TextView title;
    private LinearLayout subcategoriesLayout;

    public CategoryHolder(ViewGroup viewGroup) {
        super(viewGroup, R.layout.item_drawer_category);
        layout = (ExpandableLayout) itemView;
        layout.getHeaderLayout().setOnClickListener(CategoryHolder.this);
        icon = (IconTextView) itemView.findViewById(R.id.drawer_item_icon);
        title = (TextView) itemView.findViewById(R.id.drawer_item_title);
        subcategoriesLayout = (LinearLayout) itemView.findViewById(R.id.drawer_item_subcategories);
    }

    @Override
    public void populate(CategoryItem item) {
        super.populate(item);
        itemView.setSelected(item.isSelected());
        Iconify.setIcon(icon, item.getIcon());
        title.setText(item.getCategory().getName());

        populateSubcategories(subcategoriesLayout, item);
        if (item.isSelected()) {
            title.setTypeface(null, Typeface.BOLD);
            if (item.getCategory().getSubcategories() != null && item.getCategory().getSubcategories().size() > 0) {
                layout.show();
            } else {
                layout.hide();
            }
        } else {
            title.setTypeface(null, Typeface.NORMAL);
            layout.hide();
        }
    }

    private void populateSubcategories(ViewGroup viewGroup, CategoryItem item) {
        viewGroup.removeAllViewsInLayout();
        for (int i = 0; i < item.getCategory().getSubcategories().size(); i++) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_drawer_subcategory, viewGroup, false);
            view.setOnClickListener(new SubcategoryListener(viewGroup, i));
            Subcategory subcategory = (Subcategory) item.getCategory().getSubcategories().get(i);
            ((TextView) view.findViewById(R.id.drawer_item_title)).setText(subcategory.getName());
            if (item.getCategory().getSubcategoryPosition() == i) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
            viewGroup.addView(view, i);
        }
    }

    @Override
    public void onClick(View v) {
        if (item.isSelected()) {
            return;
        }
        item.onSelectCategory();
    }

    private class SubcategoryListener implements View.OnClickListener {

        private ViewGroup viewGroup;
        private int position;

        public SubcategoryListener(ViewGroup viewGroup, int position) {
            this.viewGroup = viewGroup;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (viewGroup.getChildAt(position).isSelected()) {
                return;
            }
            viewGroup.getChildAt(item.getCategory().getSubcategoryPosition()).setSelected(false);
            item.getCategory().setSubcategoryPosition(position);
            viewGroup.getChildAt(item.getCategory().getSubcategoryPosition()).setSelected(true);
            item.onSelectSubcategory();
        }
    }
}