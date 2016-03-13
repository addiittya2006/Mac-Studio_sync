package se.popcorn_time.mobile.ui.drawer.item;

import com.joanzapata.android.iconify.Iconify;

import se.popcorn_time.base.model.content.category.Category;
import se.popcorn_time.mobile.ui.drawer.DrawerAdapter;
import se.popcorn_time.mobile.ui.recycler.RecyclerItem;

public abstract class CategoryItem extends RecyclerItem {

    private Iconify.IconValue icon;
    private Category category;
    private boolean selected;

    public CategoryItem(Iconify.IconValue icon, Category category) {
        super(DrawerAdapter.TYPE_CATEGORY);
        this.icon = icon;
        this.category = category;
    }

    public abstract void onSelectCategory();

    public abstract void onSelectSubcategory();

    public Iconify.IconValue getIcon() {
        return icon;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}