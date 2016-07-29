package com.moodnooz;

import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SearchResultAdapter extends ArrayAdapter<Document> {

	public static final String TAG = SearchResultAdapter.class.getSimpleName();
	List<Document> results;
	LayoutInflater inflater;
	
	public SearchResultAdapter(Activity activity, List<Document> source) {
		super(activity, R.layout.search_result_item, source);
		results = source;
		inflater = LayoutInflater.from(activity);
	}
	
	@Override
	public int getCount() {
		return results.size();
	}
	
	@Override
	public Document getItem(int pos) {
		return results.get(pos);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		
		if(convertView != null && position != 0) {
			holder = (ViewHolder) convertView.getTag();
			Log.i(TAG, "convert view is not null for position " + position);
		} else {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.search_result_item, parent, false);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.description = (TextView) convertView.findViewById(R.id.description);
			holder.source = (TextView) convertView.findViewById(R.id.source);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			if(convertView != null) convertView.setTag(holder);
		}
		
		if (holder == null) {
			Log.e(TAG, "holder is null!");
			return null;
		}
		
		// bind data
		Document doc = getItem(position);
		holder.title.setText(doc.title);
		holder.description.setText(doc.description);
		holder.source.setText(doc.source);
		holder.date.setText(doc.date);
		
		return convertView;
	}
		
	class ViewHolder {
		TextView title;
		TextView description;
		TextView source;
		TextView date;
	}
}
