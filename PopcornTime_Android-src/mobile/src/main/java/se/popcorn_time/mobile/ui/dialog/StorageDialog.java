package se.popcorn_time.mobile.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.player.dialog.ListItemEntity;

import java.io.File;
import java.util.List;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.storage.StorageMount;
import se.popcorn_time.base.storage.StorageUtil;

public class StorageDialog extends DialogFragment {

    private String title;
    private List<ListItemEntity<StorageMount>> list;
    private File selectedDir;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setAdapter(new StorageAdapter(getActivity(), list), null);
        builder.setNeutralButton(android.R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public void show(FragmentManager fm, String title, List<ListItemEntity<StorageMount>> list, File selectedDir) {
        if (!isAdded() && !fm.isDestroyed()) {
            this.title = title;
            this.list = list;
            this.selectedDir = selectedDir;
            this.show(fm, "storage_dialog_" + hashCode());
        }
    }

    private class StorageAdapter extends ArrayAdapter<ListItemEntity<StorageMount>> {

        private LayoutInflater inflater;

        public StorageAdapter(Context context, List<ListItemEntity<StorageMount>> list) {
            super(context, R.layout.list_item_two_line_choice, list);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            StorageViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_two_line_choice, parent, false);
                holder = new StorageViewHolder();
                holder.lineOne = (TextView) convertView.findViewById(R.id.line1);
                holder.lineTwo = (TextView) convertView.findViewById(R.id.line2);
                holder.radioButton = (RadioButton) convertView.findViewById(R.id.radioButton);
                convertView.setTag(holder);
            } else {
                holder = (StorageViewHolder) convertView.getTag();
            }

            StorageMount storage = getItem(position).getValue();
            if (storage.primary) {
                holder.lineOne.setText(R.string.device_storage);
            } else {
                holder.lineOne.setText(getString(R.string.sdcard_storage) + ": " + storage.label);
            }
            holder.lineTwo.setText(getString(R.string.size) + ": " + StorageUtil.getSizeText(StorageUtil.getAvailableSpaceInBytes(storage.dir.getAbsolutePath())));
            if (selectedDir != null && selectedDir.getAbsolutePath().startsWith(storage.dir.getAbsolutePath())) {
                holder.radioButton.setChecked(true);
            } else {
                holder.radioButton.setChecked(false);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getItem(position).onItemChosen();
                    dismiss();
                }
            });

            return convertView;
        }
    }

    private static class StorageViewHolder {
        public TextView lineOne;
        public TextView lineTwo;
        public RadioButton radioButton;
    }
}