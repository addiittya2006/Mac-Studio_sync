package com.moodnooz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {
	 private static String TAG = SplashActivity.class.getSimpleName();
	 private static long SLEEP_TIME = 3;    

	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  requestWindowFeature(Window.FEATURE_NO_TITLE);
	  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN); 

	  setContentView(R.layout.splash);

	  // Start timer and launch main activity
	  IntentLauncher launcher = new IntentLauncher();
	  launcher.start();
	}
	 
	 @Override
	 protected void onPause() {
		 super.onPause();
		 overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	 }

	 private class IntentLauncher extends Thread {
	  @Override
	  public void run() {
	     try {
	        // Sleeping
	        Thread.sleep(SLEEP_TIME * 1000);
	     } catch (Exception e) {
	        Log.e(TAG, e.getMessage());
	     }

	     // Start main activity
	     Intent intent = new Intent(SplashActivity.this, MainActivity.class);
	     startActivity(intent);
	  }
	}
}
