package xyz.addiittya.swiperefresh;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import xyz.addiittya.swiperefresh.adapters.ArticleAdapter;
import xyz.addiittya.swiperefresh.util.Article;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String TAG = this.getClass().getSimpleName();
    private ListView lstView;
    private RequestQueue mRequestQueue;
    private ArrayList<Article> mArrArticle;
    private ArticleAdapter va;
    private SwipeRefreshLayout swipeRefreshLayout;
    private JsonArrayRequest jar;

    private static final String url = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mArrArticle = new ArrayList<>();
        va = new ArticleAdapter(mArrArticle, this);

        lstView = (ListView) findViewById(R.id.listView);
        lstView.setAdapter(va);
        mRequestQueue = Volley.newRequestQueue(this);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(this);

        jar = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {

                    for (int i = 0; i < response.length(); i++) {

                        JSONObject pjo = (JSONObject) response.get(i);
                        String pk = pjo.getString("pk");
                        JSONObject jo = pjo.getJSONObject("fields");
                        String email = jo.getString("email");
                        Article article = new Article();
                        article.setId(pk);
                        article.setText(jo.toString());
                        article.setTitle(email);
                        mArrArticle.add(article);

                    }

                    va.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.getMessage() != null) {
                    Log.i(TAG, error.getMessage());
                }
            }
        });

        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        mRequestQueue.add(jar);
                                    }
                                });

    }

    @Override
    public void onRefresh() {
        mArrArticle.clear();
        mRequestQueue.add(jar);
    }
}