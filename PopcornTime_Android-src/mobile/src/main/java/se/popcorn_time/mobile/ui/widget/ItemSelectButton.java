package se.popcorn_time.mobile.ui.widget;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.player.dialog.ListItemEntity;
import com.player.dialog.SingleChoiceDialog;

import java.util.List;

public class ItemSelectButton extends Button implements OnClickListener {

    private FragmentManager fragmentManager;
    private int prompt;
    private List<? extends ListItemEntity> items;
    private int position = -1;
    private SingleChoiceDialog dialog = new SingleChoiceDialog();

    public ItemSelectButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(ItemSelectButton.this);
    }

    public ItemSelectButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(ItemSelectButton.this);
    }

    public ItemSelectButton(Context context) {
        super(context);
        setOnClickListener(ItemSelectButton.this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (dialog != null && dialog.isAdded()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (fragmentManager == null) {
            return;
        }
        dialog.show(fragmentManager, getContext().getString(prompt), items, items.get(position));
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void setPrompt(@StringRes int prompt) {
        this.prompt = prompt;
    }

    public void setItems(List<? extends ListItemEntity> items, int position) {
        this.items = items;
        showSelectedItem(position);
    }

    public void showSelectedItem(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            this.position = position;
            setText(items.get(position).getName());
        }
    }

    public void updateText() {
        if (items != null && position >= 0 && position < items.size()) {
            setText(items.get(position).getName());
        }
    }
}