package se.popcorn_time.mobile.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.mobile.ui.VideoActivity;

public class VideoItemListener implements OnClickListener {

    private Context context;
    private VideoInfo info;

    public VideoItemListener(Context context, VideoInfo info) {
        this.context = context;
        this.info = info;
    }

    @Override
    public void onClick(View v) {
        VideoActivity.start(context, info);
    }
}