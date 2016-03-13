package se.popcorn_time.mobile.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import dp.ws.popcorntime.R;
import se.popcorn_time.mobile.ui.VpnActivity;

public class VpnDialog extends DialogFragment {

    private DialogInterface.OnClickListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_Popcorn_VpnDialog);
        builder.setCancelable(true);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle("VPN Disabled!");
        builder.setMessage(R.string.use_vpn_to_avoid_risks);
        builder.setPositiveButton("Turn on VPN (recommended)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                VpnActivity.start(getActivity());
            }
        });
        builder.setNegativeButton(R.string.continuee, listener);

        return builder.create();
    }

    public void show(@NonNull FragmentManager fm, DialogInterface.OnClickListener listener) {
        if (isAdded() || fm.isDestroyed()) {
            return;
        }
        this.listener = listener;
        show(fm, "vpn_dialog_" + hashCode());
    }
}