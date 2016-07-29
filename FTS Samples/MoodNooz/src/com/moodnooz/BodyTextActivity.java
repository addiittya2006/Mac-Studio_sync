package com.moodnooz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class BodyTextActivity extends Activity {
	public static final String TAG = BodyTextActivity.class.getSimpleName();
	String link, simpSource;
	TextView titleView;
	TextView sourceView;
	TextView dateView;
	TextView linkView;
	TextView bodyView;
	TextView tweetView;
	View loadBodyFailView;
	View progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.body_text_layout);
        
        new GetBodyTextAsyncTask().execute();
        
        titleView = (TextView)findViewById(R.id.body_title);
        sourceView = (TextView)findViewById(R.id.body_source);
        dateView = (TextView)findViewById(R.id.body_date);
        linkView = (TextView)findViewById(R.id.body_link);
        bodyView = (TextView)findViewById(R.id.body);
        tweetView = (TextView)findViewById(R.id.body_twitter);
        loadBodyFailView = findViewById(R.id.load_body_fail_msg);
        progress = findViewById(R.id.body_progress);
        
        titleView.setText(getIntent().getStringExtra(SearchResultActivity.EXTRA_TITLE));
        simpSource = getIntent().getStringExtra(SearchResultActivity.EXTRA_SIMP_SOURCE);
        sourceView.setText(getIntent().getStringExtra(SearchResultActivity.EXTRA_SOURCE));
        dateView.setText(getIntent().getStringExtra(SearchResultActivity.EXTRA_DATE));
        link = getIntent().getStringExtra(SearchResultActivity.EXTRA_LINK);
        linkView.setMovementMethod(LinkMovementMethod.getInstance());
        linkView.setText(link);
        Drawable img = getResources().getDrawable(R.drawable.twitter);
    	img.setBounds(0, 0, 25, 25);
    	tweetView.setCompoundDrawablePadding(5);
        tweetView.setCompoundDrawables(img, null, null, null);
        tweetView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				try{
					Log.i(TAG, "tweet clicked");
					startActivity(MoodNoozUtils.findTwitterClient(getBaseContext(), link));	
				} catch(ActivityNotFoundException e) {
					Toast.makeText(BodyTextActivity.this, getResources().getString(
							R.string.twitter_not_found), Toast.LENGTH_LONG).show();
				}
			}
        });
	}
	
	class GetBodyTextAsyncTask extends AsyncTask<Void, Void, Void>{
		
		String bodyText = null;

		@Override
		protected Void doInBackground(Void... params) {
			
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(MoodNoozUtils.BASE_URL);
			HttpResponse httpResponse = null;

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
			nameValuePairs.add(new BasicNameValuePair("url", link));
			nameValuePairs.add(new BasicNameValuePair("source", simpSource));

			try {
				httpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpResponse = httpclient.execute(httpost);

				Log.i(TAG, "server response code: "
						+ httpResponse.getStatusLine().getStatusCode());

				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					String responseBody = MoodNoozUtils
							.getStringResponseData(httpResponse);
					// Log.i(TAG, "response body: \"" + responseBody + "\"");
					bodyText =  responseBody;
				}
			} catch (Exception e) {
				Log.e(TAG, "error at BodyTextActivity doInBackground()"
								+ e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {	
			progress.setVisibility(View.GONE);
			if(bodyText == null || bodyText.length() == 0) {
				loadBodyFailView.setVisibility(View.VISIBLE);
				return;
			}
			loadBodyFailView.setVisibility(View.GONE);
			
			// Log.i(TAG, "bodyText for link (" + link + "), source (" + simpSource + "): " + bodyText);
			SharedPreferences prefs = MoodNoozUtils.getSharedPreferences(getApplicationContext());
			@SuppressWarnings("unchecked")
			Map<String, String> pairs = (Map<String, String>) prefs.getAll();
			Iterator<String> iter = pairs.keySet().iterator();
			try {
				while(iter.hasNext()) {
					String key = iter.next();
					String value = pairs.get(key);
					
					String regExp = "";
					for(int i = 0; i < key.length(); i++) {
						regExp += "[" + key.charAt(i) + (char)(key.charAt(i) + 'A' - 'a') +"]";
					}
					
					Log.i(TAG, "reg exp: " + regExp);
					
					if(value.equals("e"))
						bodyText = bodyText.replaceAll(regExp, "<font color=\"#dda929\">" + key + "</font>");
					else if(value.equals("p"))
						bodyText = bodyText.replaceAll(regExp, "<font color=\"red\">" + key + "</font>");
					else if(value.equals("n"))
						bodyText = bodyText.replaceAll(regExp, "<font color=\"blue\">" + key + "</font>");
					else if(value.equals("b"))
						bodyText = bodyText.replaceAll(regExp, "<font color=\"green\">" + key + "</font>");
				}
				
				bodyView.setText(Html.fromHtml(bodyText));
			} catch (Exception e) {
				loadBodyFailView.setVisibility(View.VISIBLE);
				e.printStackTrace();
			}
		}
	}
}
