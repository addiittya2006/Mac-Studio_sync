package se.popcorn_time.base.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import se.popcorn_time.api.PopcornApi;
import se.popcorn_time.api.vpn.PopcornVpnApi;
import se.popcorn_time.api.vpn.VpnClient;
import se.popcorn_time.base.utils.Logger;
import se.popcorn_time.base.vpn.VpnManager;

public final class AppApiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {

            String action = intent.getAction();
            int type = PopcornApi.getActionType(intent.getExtras());

            Logger.debug("AppApiReceiver<onReceive>: " + action + "/" + type);

            if (PopcornApi.ACTION_VPN.equals(action)) {
                if (PopcornVpnApi.TYPE_VPN_STATUS == type) {
                    VpnClient client = AppApi.getVpnClient(intent.getExtras());
                    if (client != null) {
                        VpnManager.getInstance().updateStatus(client);
                    }
                }
            }
        }
    }
}