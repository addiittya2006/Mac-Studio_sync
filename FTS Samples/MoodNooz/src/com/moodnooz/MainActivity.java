package com.moodnooz;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static final String TAG = MainActivity.class.getSimpleName();
	public static final int tickSize = 25;
	public static final String NAME_SEARCH_STRING = "searchString";
	public static final String NAME_DATE_FILTER_STRING = "dateFilterString";
		
	EditText searchBox;
	String dateFilterString;
	String searchString;
	Dialog dateFilterDialog;
	Dialog infoDialog;
	View okBtn, gotItBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, UpdateRSSService.class));
        searchBox = (EditText) findViewById(R.id.search_box);
        if(searchBox != null) {
        	searchString = searchBox.getText().toString();
        	searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        searchButtonClicked(null);
                        return true;
                    }
                    return false;
                }
            });
        }
        dateFilterString = getString(R.string.none);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public void infoButtonClicked(View view) {
    	if(infoDialog == null) {
    		infoDialog = new Dialog(MainActivity.this, R.style.dialogstyle);
    		infoDialog.setContentView(R.layout.info_dialog);
    	}
    	
    	if(gotItBtn == null) {
    		gotItBtn = infoDialog.findViewById(R.id.got_it);
    		gotItBtn.setOnClickListener(new OnClickListener(){

    			@Override
    			public void onClick(View v) {
    		    	Log.i(TAG, "got it button clicked.");
    		    	infoDialog.dismiss();
    			}		
        	});
    	}
    	
    	infoDialog.show();
    }

    public void calendarButtonClicked(View view) {
    	if(dateFilterDialog == null) {
    		dateFilterDialog = new Dialog(MainActivity.this, R.style.dialogstyle);
        	dateFilterDialog.setContentView(R.layout.date_filter_dialog);
    	}
    	
    	if(okBtn == null) {
        	okBtn = dateFilterDialog.findViewById(R.id.date_ok);
        	okBtn.setOnClickListener(new OnClickListener(){

    			@Override
    			public void onClick(View v) {
    		    	Log.i(TAG, "ok button clicked.");
    				dateFilterDialog.dismiss();
    			}		
        	});
    	}

    	final Drawable img = MainActivity.this.getResources().getDrawable(R.drawable.tick);
    	img.setBounds(0, 0, tickSize, tickSize);
    	final Vector<TextView> dateChoices = new Vector<TextView>();
    	
    	TextView today = (TextView) dateFilterDialog.findViewById(R.id.today);
    	TextView this_week = (TextView) dateFilterDialog.findViewById(R.id.this_week);
    	TextView this_month = (TextView) dateFilterDialog.findViewById(R.id.this_month);
    	TextView this_year = (TextView) dateFilterDialog.findViewById(R.id.this_year);
    	TextView none = (TextView) dateFilterDialog.findViewById(R.id.none);
    	
    	if(dateFilterString.equals(getString(R.string.none)))
    		none.setCompoundDrawables(null , null, img, null);
    	
    	dateChoices.add(today);
    	dateChoices.add(this_week);
    	dateChoices.add(this_month);
    	dateChoices.add(this_year);
    	dateChoices.add(none);
    	
    	View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i(TAG, "date chosen.");
				
				TextView chosen = (TextView) v;
				if(chosen == null) {
					Log.i(TAG, "chosen date view is null!");
					return;
				}
		    	
				for(TextView tv : dateChoices) {
					if(tv != null)
						tv.setCompoundDrawables(null, null, null, null);
				}
				
		    	chosen.setCompoundDrawables(null , null, img, null);
		    	
				dateFilterString = chosen.getText().toString();
			}
		};
    	
    	for(TextView tv : dateChoices) {
    		if(tv != null)
    			tv.setOnClickListener(listener);
    	}
    	
    	dateFilterDialog.show();
    }    
    
    @SuppressLint("DefaultLocale")
	public void searchButtonClicked(View view) {
    	searchString = searchBox.getText().toString();
    	searchString = getSanitizedQueryString(searchString.toLowerCase());
    	Log.i(TAG, "date: " + dateFilterString + ", search string: " + searchString);
    	if(searchString == null || searchString.trim().length() == 0) {
    		Toast.makeText(
					MainActivity.this,
					"Search string must contain some words.",
					Toast.LENGTH_LONG).show();
    	} else if(!MoodNoozUtils.isNetworkAvailable(getApplicationContext())) {
    		Toast.makeText(
					MainActivity.this,
					"Network is not available. Please check internet connection.",
					Toast.LENGTH_LONG).show();
    	} else {
    		Intent resultIntent = new Intent(this, SearchResultActivity.class);
    		resultIntent.putExtra(NAME_SEARCH_STRING, searchString);
    		resultIntent.putExtra(NAME_DATE_FILTER_STRING, dateFilterString);
    		startActivity(resultIntent);
    	}
    }
    
	private String getSanitizedQueryString(String searchString) {
		if (searchString == null)
			return null;

		String sanitizedString = "";
		int count = 0;
		char current;

		// remove non-alphanumeric characters except for '+', '-' and '?' if
		// they proceed a word
		while (count < searchString.length()) {
			current = searchString.charAt(count);
			if (isAlphanumeric(current) || current == ' ') {
				sanitizedString = sanitizedString + current;
			} else if (current == '+' || current == '-' || current == '?') {
				if (isAlphanumeric(searchString.charAt(count + 1)))
					sanitizedString = sanitizedString + current;
			}
			count++;
		}

		// remove duplicated words
		String[] tokens = sanitizedString.split(" ");
		Set<String> set = new HashSet<String>();
		for (String token : tokens) {
			set.add(token);
		}

		sanitizedString = "";
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			sanitizedString += iter.next() + " ";
		}

		Log.i(TAG, "sanitized string: " + sanitizedString);
		return sanitizedString;
	}

	private static boolean isAlphanumeric(char c) {
		return ('a' <= c && c <= 'z') || ('0' <= c && c <= '9');
	}
}
