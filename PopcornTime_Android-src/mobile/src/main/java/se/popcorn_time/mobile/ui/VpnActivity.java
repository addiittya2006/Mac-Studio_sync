package se.popcorn_time.mobile.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collection;

import dp.ws.popcorntime.R;
import se.popcorn_time.api.vpn.VpnClient;
import se.popcorn_time.base.api.AppApi;
import se.popcorn_time.base.prefs.PopcornPrefs;
import se.popcorn_time.base.prefs.Prefs;
import se.popcorn_time.base.vpn.VpnManager;
import se.popcorn_time.mobile.ui.base.PopcornBaseActivity;
import se.popcorn_time.mobile.ui.recycler.SingleItemTouchListener;
import se.popcorn_time.mobile.ui.settings.SettingsAdapter;
import se.popcorn_time.mobile.ui.settings.item.SettingsActionItem;
import se.popcorn_time.mobile.ui.settings.item.SettingsCheckItem;
import se.popcorn_time.mobile.ui.settings.item.SettingsHeaderItem;

public final class VpnActivity extends PopcornBaseActivity {

    private SettingsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Popcorn_Classic);
        super.onCreate(savedInstanceState);

        VpnManager.getInstance().clearClients();
        VpnManager.getInstance().addVpnListener(vpnListener);

        // Toolbar
        getPopcornLogoView().setVisibility(View.GONE);
        getPopcornTitle().setVisibility(View.VISIBLE);

        // Content
        adapter = new SettingsAdapter();
        adapter.add(createAccount);
        RecyclerView view = (RecyclerView) setPopcornContentView(R.layout.activity_settings);
        view.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        view.addOnItemTouchListener(new SingleItemTouchListener());
        view.setAdapter(adapter);

        AppApi.getVpnStatus(VpnActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VpnManager.getInstance().removeVpnListener(vpnListener);
    }

    @Override
    public void updateLocaleText() {
        super.updateLocaleText();
        getPopcornTitle().setText(R.string.vpn);
    }

    private void updateAdapter() {
        adapter.clear();
        adapter.add(createAccount);
        Collection<VpnClient> clients = VpnManager.getInstance().getClients();
        for (VpnClient client : clients) {
            adapter.add(new SettingsHeaderItem(client.getName()));
            adapter.add(new VpnConnectItem(client));
            adapter.add(new ConnectOnStartItem(client));
        }
        adapter.notifyDataSetChanged();
    }

    private final VpnManager.VpnListener vpnListener = new VpnManager.VpnListener() {
        @Override
        public void onStatusUpdated() {
            updateAdapter();
        }
    };

    private final SettingsActionItem createAccount = new SettingsActionItem() {
        @Override
        public String getTitle() {
            return getString(R.string.create_account);
        }

        @Override
        public String getSubtitle() {
            return "Click here to create your VPN account";
        }

        @Override
        public void onAction() {
            String[] providers = VpnManager.getInstance().getProviders();
            if (providers != null && providers.length > 0) {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(providers[0]));
                browser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browser);
            }
        }
    };

    protected final class VpnConnectItem extends SettingsActionItem {

        private VpnClient client;

        public VpnConnectItem(VpnClient client) {
            this.client = client;
        }

        @Override
        public String getTitle() {
            return getString(VpnClient.STATUS_CONNECTED == client.getStatus() ? R.string.disconnect : R.string.connect);
        }

        @Override
        public String getSubtitle() {
            return getString(VpnClient.STATUS_CONNECTED == client.getStatus() ? R.string.vpn_connected : R.string.vpn_not_connected);
        }

        @Override
        public void onAction() {
            if (VpnClient.STATUS_CONNECTED == client.getStatus()) {
                AppApi.disconnectVpn(VpnActivity.this, client);
            } else if (VpnClient.STATUS_DISCONNECTED == client.getStatus()) {
                AppApi.connectVpn(VpnActivity.this, client);
            }
        }
    }

    protected final class ConnectOnStartItem extends SettingsCheckItem {

        private VpnClient client;

        public ConnectOnStartItem(VpnClient client) {
            this.client = client;
        }

        @Override
        public String getTitle() {
            return getString(R.string.connect_on_start);
        }

        @Override
        public String getSubtitle() {
            return getString(isChecked() ? R.string.enabled : R.string.disabled);
        }

        @Override
        public boolean isChecked() {
            String onStartVpnPackage = Prefs.getPopcornPrefs().get(PopcornPrefs.ON_START_VPN_PACKAGE, "");
            return onStartVpnPackage != null && onStartVpnPackage.equals(client.getPackageName());
        }

        @Override
        public void onAction() {
            if (isChecked()) {
                Prefs.getPopcornPrefs().put(PopcornPrefs.ON_START_VPN_PACKAGE, "");
            } else {
                Prefs.getPopcornPrefs().put(PopcornPrefs.ON_START_VPN_PACKAGE, client.getPackageName());
            }
            adapter.notifyDataSetChanged();
        }
    }

    public static void start(@NonNull Context context) {
        context.startActivity(new Intent(context, VpnActivity.class));
    }
}