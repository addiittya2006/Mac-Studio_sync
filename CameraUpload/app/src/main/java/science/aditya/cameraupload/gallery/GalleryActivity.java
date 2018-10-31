package science.aditya.cameraupload.gallery;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import science.aditya.cameraupload.R;

public class GalleryActivity extends AppCompatActivity {
    SwipeRefreshLayout swipeLayout;
    ListView listView;
    private RequestQueue requestQueue;
    private ArrayList<CDNImage> arrayList;
    private GalleryListAdapter galleryListAdapter;
    private JsonObjectRequest jsonObjectRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        arrayList = new ArrayList<>();
        galleryListAdapter = new GalleryListAdapter(this, arrayList);

        requestQueue = Volley.newRequestQueue(this);

        listView = findViewById(R.id.listView);
        listView.setAdapter(galleryListAdapter);

        swipeLayout = findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        swipeLayout.setRefreshing(true);
                        getCDNData();
                    }
                }, 1000); // Delay in millis
            }
        });

        if(null == savedInstanceState) {
            swipeLayout.setRefreshing(true);
            getCDNData();
        }

    }

    void getCDNData() {
        jsonObjectRequest = new JsonObjectRequest(JsonRequest.Method.GET, "https://www.googleapis.com/storage/v1/b/mp_cam_images/o", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray arrayImages = response.getJSONArray("items");
                    arrayList.clear();
                    for (int i = 0; i < arrayImages.length(); i++) {
                        JSONObject curObj = arrayImages.getJSONObject(i);
                        CDNImage cdnImage = new CDNImage();
                        cdnImage.setImageName(curObj.getString("name"));
                        cdnImage.setImageURL(curObj.getString("mediaLink"));
                        arrayList.add(cdnImage);
                    }
                    galleryListAdapter.notifyDataSetChanged();
                    swipeLayout.setRefreshing(false);
                } catch (JSONException e) {
                    Log.d("hell", e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("hell", error.getMessage());
                Toast.makeText(GalleryActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

}
