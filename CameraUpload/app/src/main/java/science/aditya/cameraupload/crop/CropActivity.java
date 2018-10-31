package science.aditya.cameraupload.crop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import science.aditya.cameraupload.R;
import science.aditya.cameraupload.gallery.GalleryActivity;

public class CropActivity extends AppCompatActivity implements CropImageView.OnCropImageCompleteListener {

    Uri picUri;
    Bitmap cropped;
    String uploadFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        if(null == savedInstanceState){
            Intent intent = getIntent();
            picUri = Uri.parse(intent.getStringExtra("picUri"));
        }

        final CropImageView cropImageView = findViewById(R.id.cropImageView);
        cropImageView.setImageUriAsync(picUri);
        Button uploadButton = findViewById(R.id.upload_button);
        Button discardButton = findViewById(R.id.discard_button);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.getCroppedImageAsync();
            }
        });

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cropImageView.setOnCropImageCompleteListener(this);

    }

    private String savefile(Bitmap bm) {
//        File dir = new File(Environment.getExternalStorageDirectory().getPath()+"//Pictures//mpcu");
        File dir = getFilesDir();
        if(!dir.exists()){
            dir.mkdirs();
        }
//        Random generator = new Random();
//        int n = 10000;
//        n = generator.nextInt(n);
//        String fname = "IMG_" + n + ".jpg";
//        File file = new File(dir, fname);
        File file = new File(dir, "cropped.jpg");
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 70, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    private void uploadImage(String fileName) {

        File file = new File(fileName);

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String cdnNname = "IMG_" + n + ".jpg";

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), file);

        Request request = new Request.Builder()
                .url("https://www.googleapis.com/upload/storage/v1/b/mp_cam_images/o?uploadType=media"+"&name="+cdnNname)
                .addHeader("Content-Type", "image/jpeg")
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.d("hell", response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private class UploadTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public UploadTask(CropActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Uploading...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Intent i = new Intent(CropActivity.this, GalleryActivity.class);
            startActivity(i);
            finish();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                uploadImage(uploadFileName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        cropped = view.getCroppedImage();
        uploadFileName = savefile(cropped);
        new UploadTask(this).execute();
    }
}
