package xyz.addiittya.webviewtest;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity {

		// set your custom url here
//		String url = "http://www.google.com";
		String url;
		// if you want to show progress bar on splash screen
//		Boolean showProgressOnSplashScreen = true;

		WebView mWebView;
		ProgressBar prgs;
		RelativeLayout splash, main_layout;

	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			this.getWindow().requestFeature(Window.FEATURE_PROGRESS);

			setContentView(R.layout.activity_main);
			getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
					Window.PROGRESS_VISIBILITY_ON);

			mWebView = (WebView) findViewById(R.id.wv);
			prgs = (ProgressBar) findViewById(R.id.progressBar);
			main_layout = (RelativeLayout) findViewById(R.id.main_layout);

			// splash screen View
//			if (!showProgressOnSplashScreen)
//				((ProgressBar) findViewById(R.id.progressBarSplash)).setVisibility(View.GONE);
//			splash = (RelativeLayout) findViewById(R.id.splash);

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//
//			// get status bar height to push webview below that
//			int result = 0;
//			int resourceId = getResources().getIdentifier("status_bar_height",
//					"dimen", "android");
//			if (resourceId > 0) {
//				result = getResources().getDimensionPixelSize(resourceId);
//			}
//
//			// set top padding to status bar
//			main_layout.setPadding(0, result, 0, 0);
//		}

			url = getIntent().getStringExtra("url");
			mWebView.loadUrl(url);

			// control javaScript and add html5 features
			mWebView.setFocusable(true);
			mWebView.setFocusableInTouchMode(true);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
//            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			mWebView.getSettings().setDomStorageEnabled(true);
			mWebView.getSettings().setAppCacheEnabled(true);
			mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
			mWebView.getSettings().setDatabaseEnabled(true);
			mWebView.getSettings().setDatabasePath(
					this.getFilesDir().getPath() + this.getPackageName()
							+ "/databases/");

			// this force use chromeWebClient
			mWebView.getSettings().setSupportMultipleWindows(true);
			mWebView.setWebViewClient(new WebViewClient() {

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return false;
				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					if (prgs.getVisibility() == View.GONE) {
						prgs.setVisibility(View.VISIBLE);
					}
				}

				@Override
				public void onLoadResource(WebView view, String url) {
					super.onLoadResource(view, url);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);

					if (prgs.getVisibility() == View.VISIBLE)
						prgs.setVisibility(View.GONE);

//					mWebView.saveWebArchive(getFilesDir().getAbsolutePath() + File.separator + "saved_page");
					mWebView.saveWebArchive(getFilesDir().getAbsolutePath() + File.separator + "saved_page.mht");
//					Log.i("file:", getFilesDir().getAbsolutePath() + File.separator + "saved_page");

					// check if splash is still there, get it away!
//					if (splash.getVisibility() == View.VISIBLE)
//						splash.setVisibility(View.GONE);
					// slideToBottom(splash);

				}

			});

		}

	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		finish();
	}

	/**
		 * To animate view slide out from top to bottom
		 *
		 * @param view
		 */
		// void slideToBottom(View view) {
		// TranslateAnimation animate = new TranslateAnimation(0, 0, 0,
		// view.getHeight());
		// animate.setDuration(2000);
		// animate.setFillAfter(true);
		// view.startAnimation(animate);
		// view.setVisibility(View.GONE);
		// }

		@Override
		public boolean onKeyDown(final int keyCode, final KeyEvent event) {
//			if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
//				mWebView.goBack();
//				return true;
//			}
			return super.onKeyDown(keyCode, event);
		}

}
