package se.popcorn_time.mobile.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.model.video.info.VideoInfo;

public class VideoAdapter extends BaseAdapter {

    private Context context;
    private List<VideoInfo> data;
    private LayoutInflater inflater;

    public VideoAdapter(Context context, List<VideoInfo> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public VideoInfo getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VideoHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_grid_video, parent, false);
            holder = new VideoHolder();
            holder.poster = (RoundedImageView) convertView.findViewById(R.id.video_poster);
            holder.name = (TextView) convertView.findViewById(R.id.video_name);
            holder.year = (TextView) convertView.findViewById(R.id.video_year);
            convertView.setTag(holder);
        } else {
            holder = (VideoHolder) convertView.getTag();
        }

        VideoInfo info = getItem(position);
        holder.poster.setOnClickListener(new VideoItemListener(context, info));
        holder.poster.setOnLongClickListener(new FavoritesListener(context, info));
        Picasso.with(context).load(info.posterMediumUrl).placeholder(R.drawable.poster).into(holder.poster);
        holder.name.setText(Html.fromHtml("<b>" + info.title + "</b>"));
        holder.year.setText(info.year);

        return convertView;
    }

    public void addData(List<VideoInfo> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }
}
