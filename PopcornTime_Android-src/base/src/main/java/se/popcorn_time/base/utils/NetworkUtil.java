package se.popcorn_time.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.prefs.SettingsPrefs;
import se.popcorn_time.base.receiver.ConnectivityReceiver;

public class NetworkUtil {

    public static boolean hasAvailableConnection(Context context) {
        boolean onlyWifi = Prefs.getSettingsPrefs().get(SettingsPrefs.ONLY_WIFI_CONNECTION, ConnectivityReceiver.DEFAULT_ONLY_WIFI_CONNECTION);
        if (onlyWifi) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && ConnectivityManager.TYPE_WIFI == activeNetwork.getType();
        }
        return true;
    }
}