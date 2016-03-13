package com.player.subtitles;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.util.Map;

public class SubtitlesRenderer {

    public static final String[] SUPPORTED_EXTENSIONS = new String[]{
            FormatSRT.EXTENSION
    };

    private TextView subtitlesView;
    private Map<Integer, Caption> captions;
    private int index;

    public SubtitlesRenderer() {

    }

    public void setSubtitlesView(TextView subtitlesView) {
        this.subtitlesView = subtitlesView;
    }

    public Map<Integer, Caption> getCaptions() {
        return captions;
    }

    public void disable() {
        captions = null;
        if (subtitlesView != null) {
            subtitlesView.setVisibility(View.GONE);
        }
    }

    public void setSubtitlesColor(int color) {
        if (subtitlesView != null) {
            subtitlesView.setTextColor(color);
        }
    }

    public void setSubtitlesSize(float size) {
        if (subtitlesView != null) {
            subtitlesView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    public void setSubtitlesTrack(@NonNull final Format format, @NonNull final BufferedReader reader, final TextFormatter formatter) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    captions = format.parse(reader, formatter);
                    index = 1;
                } catch (Exception e) {
                    captions = null;
                    e.printStackTrace();
                }
            }
        });
    }

    public void onUpdate(long timeMillis) {
        if (captions != null && subtitlesView != null) {
            Caption caption;
            if (captions.containsKey(index)) {
                if (timeMillis >= captions.get(index).startMillis && timeMillis <= captions.get(index).endMillis) {
                    caption = captions.get(index);
                } else {
                    caption = searchCaption(timeMillis);
                }
            } else {
                caption = searchCaption(timeMillis);
            }
            if (caption != null) {
                subtitlesView.setText(caption.content);
                subtitlesView.setVisibility(View.VISIBLE);
            } else {
                subtitlesView.setVisibility(View.GONE);
            }
        }
    }

    @Nullable
    private Caption searchCaption(long timeMillis) {
        for (Integer key : captions.keySet()) {
            if (timeMillis >= captions.get(key).startMillis && timeMillis <= captions.get(key).endMillis) {
                index = key;
                return captions.get(key);
            }
        }
        return null;
    }
}