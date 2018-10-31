package science.aditya.cameraupload.gallery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import science.aditya.cameraupload.R;
import science.aditya.cameraupload.preview.ImagePreviewActivity;

public class GalleryListAdapter extends BaseAdapter {

    Context c;
    ArrayList<CDNImage> images;
    LayoutInflater inflater;

    public GalleryListAdapter(Context c, ArrayList<CDNImage> images) {
        this.c = c;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(inflater==null) {
            inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if(convertView==null) {
            convertView=inflater.inflate(R.layout.gallery_item,parent,false);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(c, ImagePreviewActivity.class);
                    i.putExtra("picUrl", images.get(position).getImageURL());
                    c.startActivity(i);
                }
            });
        }

        GalleryItemViewHolder holder=new GalleryItemViewHolder(convertView);
        holder.nameTV.setText(images.get(position).getImageName());
        Glide.with(c).load(images.get(position).getImageURL()).into(holder.thumb);

        return convertView;
    }

}
