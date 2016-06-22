package xyz.addiittya.webviewtest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LaunchActivity extends AppCompatActivity {

	String url = "http://www.google.com";
	private CustomTabsClient mClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);

		CustomTabsServiceConnection mConnection = new CustomTabsServiceConnection() {
			@Override
			public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient customTabsClient) {
				mClient = customTabsClient;
			}

			@Override
			public void onServiceDisconnected(ComponentName componentName) {
				mClient = null;
			}
		};

//		String packageName = "com.android.dev";
//		Log.i("Jell", isChromeAvailable());
		if (!isChromeAvailable().equals("nf"))
			CustomTabsClient.bindCustomTabsService(this, isChromeAvailable(), mConnection);

//		mClient.bindCustomTabsService(this, "com.chrome.dev", mConnection);

	}

	private String isChromeAvailable() {
		String chrome = "com.android.chrome";
		String beta = "com.chrome.beta";
		String dev = "com.chrome.dev";
		if (appInstalledOrNot(dev))
			return dev;
		else if (appInstalledOrNot(beta))
			return beta;
//		else if (appInstalledOrNot(chrome))
//			return chrome;
		else return "nf";
	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	public void goToWebView() throws IOException {
		Intent i = new Intent(this, MainActivity.class);
		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
		if (cd.isConnectingToInternet()) {
			i.putExtra("url", "https://www.google.com");
			startActivity(i);
		} else {
//			if(getFilesDir().getAbsolutePath() + File.separator + "saved_page.mht")
//			File f = getFileStreamPath(getFilesDir().getAbsolutePath() + File.separator + "saved_page.mht");
//			File f = getFileStreamPath(getFilesDir().getAbsolutePath() + "saved_page.mht");
			FileInputStream fis = new FileInputStream (new File(getFilesDir().getAbsolutePath() + File.separator + "saved_page.mht"));
			if (fis.read() == 0) {
				// empty or doesn't exist
				Toast.makeText(this, "NO Internet", Toast.LENGTH_SHORT).show();
			} else {
				// exists and is not empty
				i.putExtra("url", "file:///" + getFilesDir().getAbsolutePath() + File.separator + "saved_page.mht");
				startActivity(i);
			}
//			i.putExtra("url", "file:///" + getFilesDir().getAbsolutePath() + File.separator + "saved_page.mht");
//			Toast.makeText(this, "NO Internet", Toast.LENGTH_SHORT).show();
		}
	}

	public void loadPage(View view) {
		if (!isChromeAvailable().equals("nf"))
//			loadCustomTabs();
			Log.d("Hell", isChromeAvailable());
		else try {
			goToWebView();
		} catch (IOException e) {
//			e.printStackTrace();
			Toast.makeText(this, "NO Internet", Toast.LENGTH_SHORT).show();
		}
	}

	public void loadCustomTabs() {
		CustomTabsIntent.Builder mBuilder = new CustomTabsIntent.Builder();
//		CustomTabsIntent.Builder mBuilder = new CustomTabsIntent.Builder(getSession());
		mBuilder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
		CustomTabsIntent mIntent = mBuilder.build();
		mIntent.launchUrl(this, Uri.parse(url));
	}

//	private CustomTabsSession getSession() {
//		return mClient.newSession(new CustomTabsCallback() {
//			@Override
//			public void onNavigationEvent(int navigationEvent, Bundle extras) {
//				super.onNavigationEvent(navigationEvent, extras);
//			}
//		});
//	}
}