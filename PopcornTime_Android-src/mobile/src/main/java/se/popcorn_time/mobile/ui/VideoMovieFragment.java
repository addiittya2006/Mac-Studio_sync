package se.popcorn_time.mobile.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

import dp.ws.popcorntime.R;
import se.popcorn_time.mobile.ui.base.VideoTypeFragment;

public class VideoMovieFragment extends VideoTypeFragment {

    private TextView actors;
    private ImageButton trailer;
    private TextView trailerText;

    public VideoMovieFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_video_movie, container, false);
        populateView(view);
        restartSubtitlesLoader();
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onChangeScreenOrientation(R.layout.fragment_video_movie);
    }

    @Override
    protected void populateView(View view) {
        super.populateView(view);
        actors = (TextView) view.findViewById(R.id.video_movie_actors);
        trailer = (ImageButton) view.findViewById(R.id.video_trailer);
        trailerText = (TextView) view.findViewById(R.id.video_trailer_text);
        if (TextUtils.isEmpty(videoInfo.actors)) {
            actors.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(videoInfo.trailer)) {
            trailer.setVisibility(View.GONE);
            trailerText.setVisibility(View.GONE);
        } else {
            trailer.setOnClickListener(trailerListener);
            trailer.setOnTouchListener(trailerTouchListener);
            trailerText.setOnClickListener(trailerListener);
            trailerText.setOnTouchListener(trailerTouchListener);
        }
        updateLocaleText();
    }

    @Override
    public void updateLocaleText() {
        super.updateLocaleText();
        actors.setText(getString(R.string.actors) + ":\n" + Html.fromHtml(videoInfo.actors));
        trailerText.setText(R.string.trailer);
        updateTorrents();
    }

    @Override
    protected void sendCustomSubtitle(File file) {

    }

    private OnClickListener trailerListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            TrailerActivity.start(getActivity(), videoInfo.trailer);
        }
    };

    private OnTouchListener trailerTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (MotionEvent.ACTION_DOWN == action) {
                trailerTouch(v.getId(), true);
            } else if (MotionEvent.ACTION_UP == action) {
                trailerTouch(v.getId(), false);
            }

            return false;
        }

        private void trailerTouch(int id, boolean isPressed) {
            if (R.id.video_trailer == id) {
                trailerText.setPressed(isPressed);
            } else if (R.id.video_trailer_text == id) {
                trailer.setPressed(isPressed);
            }
        }
    };
}