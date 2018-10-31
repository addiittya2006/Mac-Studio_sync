package science.aditya.cameraupload.preview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.bumptech.glide.Glide;

import science.aditya.cameraupload.R;
import science.aditya.cameraupload.preview.gestureview.GestureImageView;

public class ImagePreviewActivity extends AppCompatActivity implements OnClickListener {

    GestureImageView fullscreenIV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fullscreenIV = findViewById(R.id.fullscreenIV);

        String picUrl = getIntent().getStringExtra("picUrl");

        if (picUrl != null) {

            Glide.with(this)
                    .load(picUrl)
                    .into(fullscreenIV);

            fullscreenIV.setOnClickListener(this);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fullscreenIV)
            this.onClick(fullscreenIV);
    };

}
