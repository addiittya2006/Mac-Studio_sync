package se.popcorn_time.base.vpn;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.popcorn_time.api.vpn.VpnClient;
import se.popcorn_time.base.api.AppApi;
import se.popcorn_time.base.config.Configuration;
import se.popcorn_time.base.prefs.PopcornPrefs;
import se.popcorn_time.base.prefs.Prefs;

public final class VpnManager {

    public interface VpnListener {
        void onStatusUpdated();
    }

    private final static VpnManager INSTANCE = new VpnManager();

    public String[] providers;

    private final Set<VpnListener> listeners = new HashSet<>();
    private Map<String, VpnClient> clients = new HashMap<>();

    private VpnClient activeClient;

    private VpnManager() {
    }

    public static VpnManager getInstance() {
        return INSTANCE;
    }

    public void addVpnListener(@NonNull VpnListener listener) {
        listeners.add(listener);
    }

    public void removeVpnListener(@NonNull VpnListener listener) {
        listeners.remove(listener);
    }

    public void clearVpnListeners() {
        listeners.clear();
    }

    public boolean isHaveProviders() {
        return providers != null && providers.length > 0;
    }

    public void loadProviders() {
        if (Prefs.getPopcornPrefs().contains(PopcornPrefs.VPN_PROVIDERS)) {
            Set<String> providers = Prefs.getPopcornPrefs().get(PopcornPrefs.VPN_PROVIDERS, new HashSet<String>());
            this.providers = providers.toArray(new String[providers.size()]);
        }
    }

    @Nullable
    public String[] getProviders() {
        return providers;
    }

    public void setProviders(String[] providers) {
        this.providers = providers;
    }

    public boolean isConnected() {
        return activeClient != null && VpnClient.STATUS_CONNECTED == activeClient.getStatus();
    }

    @NonNull
    public Collection<VpnClient> getClients() {
        return clients.values();
    }

    @Nullable
    public VpnClient getActiveClient() {
        return activeClient;
    }

    public void clearClients() {
        clients.clear();
    }

    public void updateStatus(@NonNull VpnClient client) {
        clients.put(client.getPackageName(), client);

        if (VpnClient.STATUS_CONNECTED == client.getStatus()) {
            if (activeClient == null) {
                activeClient = client;
            }
        } else if (VpnClient.STATUS_DISCONNECTED == client.getStatus()) {
            if (activeClient != null && activeClient.getPackageName().equals(client.getPackageName())) {
                activeClient = null;
            }
        }

        for (VpnListener listener : listeners) {
            listener.onStatusUpdated();
        }
    }

    public void connectOnStart(@NonNull Context context) {
        String onStartVpnPackage = Prefs.getPopcornPrefs().get(PopcornPrefs.ON_START_VPN_PACKAGE, "");
        if (TextUtils.isEmpty(onStartVpnPackage)) {
            return;
        }
        for (VpnClient client : clients.values()) {
            if (onStartVpnPackage.equals(client.getPackageName())) {
                if (VpnClient.STATUS_DISCONNECTED == client.getStatus()) {
                    AppApi.connectVpn(context, client);
                }
                break;
            }
        }
    }
}