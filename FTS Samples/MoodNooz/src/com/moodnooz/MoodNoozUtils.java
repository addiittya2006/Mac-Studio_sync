package com.moodnooz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.net.Uri;
import android.util.Log;

public class MoodNoozUtils {
	
	public static final String BASE_URL = "http://ucdmoodnooz.appspot.com/ucdmoodnooz";
	
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager conMgr =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean availability = false;
		try {
			if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() ==
				NetworkInfo.State.CONNECTED || conMgr.getNetworkInfo(ConnectivityManager
				.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
				availability = true;
			}
		} catch (Exception e) {
			return true;
		}
		return availability;
	}
	
	public static String getStringResponseData(HttpResponse httpResponse)
			throws ParseException, IOException {
		if(httpResponse == null) {
			return "httpResponse is null!";
		} else if(httpResponse.getEntity() == null) {
			return "httpResponse entity is null";
		} else {
			return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
		}
	}
	
	public static Intent findTwitterClient(Context context, String link) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, "it works");
		intent.setType("text/plain");

		String message = String.format(context.getString(R.string.tweet_msg), link);
		final PackageManager pm = context.getPackageManager();
		final List<ResolveInfo> activityList = pm.queryIntentActivities(intent, 0);
		int len = activityList.size();
		for (int i = 0; i < len; i++) {
			final ResolveInfo app = (ResolveInfo) activityList.get(i);
			if ("com.twitter.android.PostActivity".equals(app.activityInfo.name)) {

				final ActivityInfo activity = app.activityInfo;
				final ComponentName name = new ComponentName(
						activity.applicationInfo.packageName, activity.name);
				intent = new Intent(Intent.ACTION_SEND);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setComponent(name);
				intent.putExtra(Intent.EXTRA_TEXT, message);

				return intent;
			}
		}
		
		// Nothing found, launch twitter browser interface

		Uri uriUrl = Uri.parse("https://twitter.com/intent/tweet?text=" + message);
		intent = new Intent(Intent.ACTION_VIEW, uriUrl);
		final ComponentName name = new ComponentName("com.android.browser",
				"com.android.browser.BrowserActivity");
		intent.setComponent(name);
		
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
						Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
						Intent.FLAG_ACTIVITY_NO_HISTORY);
		
		return intent;
	}
	
	public static String fetchBody(String link, String source) {
		String body = null;
		
		try {
			String rawText = getRawText(link);
			Log.i("rawText", rawText);
			rawText = rawText.replace("\r", "");
			rawText = rawText.replace("\n", "");
			
			Pattern bodyPattern;
			Matcher bodyMatcher;
			StringBuilder builder = new StringBuilder();
			
			if(source.equals("a")) {
				// <p CLASS="no_name"> body </p> </section>
				
				bodyPattern = Pattern.compile("<p\\s*[Cc][Ll][Aa][Ss][Ss]\\s*=\\s*\"no_name\"\\s*>.+<\\/p>\\s*(<[^<]+>\\s*)*<\\/section>");
				bodyMatcher = bodyPattern.matcher(rawText);
				if(bodyMatcher.find()) {
					body = bodyMatcher.group();
					
					// remove image description
					body = body.replaceAll("<li\\s*[^>]*\\s>\\s*<img.+\\s*<\\s*/\\s*li\\s*>", "");	
					// remove javascript
					body = body.replaceAll("<script\\s*.+</script>", "");				
					// remove everything that is in <p class = "some_other_names"> </p>
					body = body.replaceAll("<p\\s*[Cc][Ll][Aa][Ss][Ss]\\s*=\\s*\"[^Nn]*\"\\s*>\\s*[^<>]+\\s*<\\/p>", "");
					
				}
			} else if(source.equals("b")) {
				// Guardian
				bodyPattern = Pattern.compile("<div\\s*id=\\s*\\\"article-body-blocks\\\">\\s*.+\\s*<\\/div>");
				bodyMatcher = bodyPattern.matcher(rawText);
				if(bodyMatcher.find()) {
					body = bodyMatcher.group();
					// rawBody is in a form of <div id="article-body-blocks">...<p>real_body</p>...</div>
				}
				
				bodyPattern = Pattern.compile("(<p>\\s*.+\\s*<\\/p>){3,}\\s*.+\\s*<\\/div>");
				bodyMatcher = bodyPattern.matcher(body);
				if(bodyMatcher.find()) {
					body = bodyMatcher.group();
					// rawBody is in a form of <p>...</p><p>...</p><p>...</p> ... <p>...</p>
				}
				
				int firstIndex = body.indexOf("</div>");
				body = body.substring(0, firstIndex);
				
			} else if(source.equals("c")) {
				bodyPattern = Pattern.compile("<p\\s*class=\"introduction\"[^>]*>.+<!--\\s*.*\\s*story-body\\s*-->");
				bodyMatcher = bodyPattern.matcher(rawText);
				if(bodyMatcher.find()) {
					body = bodyMatcher.group();
					bodyPattern = Pattern.compile("<p>[^<]+</p>");
					bodyMatcher = bodyPattern.matcher(body);
					
					while(bodyMatcher.find()) {
						builder = builder.append(bodyMatcher.group());
					}
				}
				body = builder.toString();
			}
			
			if(body != null) {
								
				// removes html tags
				body = body.replaceAll("</p>\\s*", "\n")
						.replaceAll("<BR/>\\s*", "\n")
						.replaceAll("<br/>\\s*", "\n")
						.replaceAll("<\\s*\\/{0,1}[^>]*>", "")
						.replaceAll("&nbsp;", " ")
						.replaceAll("&quot;", "\"")
						.replaceAll("\n\\s+", "\n") // remove consecutive spaces after a newline																			
						.replaceAll("\\s{3,}", " "); // remove consecutive spaces
				
				// decode string (Irish Times needs it)
				body = NumericCharacterReference.decode(body, '?');
			}

			return body.trim();
			
		} catch(Exception e) {}
	
		return body;
	}
	
	private static String getRawText(String link) throws IOException {

		URL url;
		url = new URL(link);

		URLConnection connection = url.openConnection();
		
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), "UTF-8"));
			
		StringBuffer stringBuffer = new StringBuffer();
		String inputLine;
		// read from buffer line by line
		while ((inputLine = bufferedReader.readLine()) != null) {
			stringBuffer.append(inputLine + "\n");
		}
		bufferedReader.close();
		return stringBuffer.toString();
	}
	
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences("moodnooz_prefs", Context.MODE_PRIVATE);
	}
}
