package se.popcorn_time.mobile.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.squareup.picasso.Picasso;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.database.tables.Favorites;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.utils.Logger;

public class FavoritesAdapter extends CursorAdapter {

    private LayoutInflater inflater;

    public FavoritesAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        VideoHolder holder = (VideoHolder) view.getTag();
        VideoInfo info = null;
        try {
            info = Favorites.create(cursor);
        } catch (Exception e) {
            Logger.debug("FavoritesAdapter: " + e.getMessage());
        }
        if (info != null) {
            holder.poster.setOnClickListener(new VideoItemListener(context, info));
            holder.poster.setOnLongClickListener(new FavoritesListener(context, info));
            Picasso.with(context).load(info.posterMediumUrl).placeholder(R.drawable.poster).into(holder.poster);
            holder.name.setText(Html.fromHtml("<b>" + info.title + "</b>"));
            holder.year.setText(info.year);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_grid_video, parent, false);
        VideoHolder holder = new VideoHolder();
        holder.poster = (RoundedImageView) view.findViewById(R.id.video_poster);
        holder.name = (TextView) view.findViewById(R.id.video_name);
        holder.year = (TextView) view.findViewById(R.id.video_year);
        view.setTag(holder);
        return view;
    }
}