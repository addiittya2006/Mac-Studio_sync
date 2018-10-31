package science.aditya.cameraupload.gallery;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import science.aditya.cameraupload.R;

public class GalleryItemViewHolder {

    TextView nameTV;
    ImageView thumb;

    public GalleryItemViewHolder(View v) {

        nameTV= (TextView) v.findViewById(R.id.nameTV);
        thumb= (ImageView) v.findViewById(R.id.thumb);

    }

}
