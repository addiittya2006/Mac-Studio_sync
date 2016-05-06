package xyz.addiittya.infinitescrollapi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.List;

import xyz.addiittya.infinitescrollapi.Utils.Question;
import xyz.addiittya.infinitescrollapi.adapters.QuestionsAdapter;
import xyz.addiittya.infinitescrollapi.classes.CustomJSONObjectRequest;
import xyz.addiittya.infinitescrollapi.classes.CustomVolleyRequestQueue;
import xyz.addiittya.infinitescrollapi.classes.EndlessScrollListener;

public class MainActivity extends AppCompatActivity implements Response.Listener, Response.ErrorListener {

    public static final String URL = "";

    public static final String REQUEST_TAG = "MainActivity";

    private RequestQueue mQueue;

    private RecyclerView rvItems = (RecyclerView) findViewById(R.id.rvQuestions);
    private TextView mTextView = (TextView) findViewById(R.id.answer_area);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONObject params= new JSONObject();
        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext())
                .getRequestQueue();

        final CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method
                .POST,URL,params
                , this, this);
        jsonRequest.setTag(REQUEST_TAG);

        //        try {
        // TODO Add Params Here
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        final List<Question> allQuestions = Question.createQuestionsList(10);
        final QuestionsAdapter adapter = new QuestionsAdapter(allQuestions);
        rvItems.setAdapter(adapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvItems.setLayoutManager(linearLayoutManager);
        rvItems.addOnScrollListener(new EndlessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
//                List<Question> moreQuestions = Question.createQuestionsList(10);
                int curSize = adapter.getItemCount();
                mQueue.add(jsonRequest);
//                allQuestions.addAll(moreQuestions);
                adapter.notifyItemRangeInserted(curSize, allQuestions.size() - 1);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mTextView.setText(error.getMessage());
    }

    @Override
    public void onResponse(Object response) {
        mTextView.setText("Response is: " + response);
        try {
            mTextView.setText(mTextView.getText() + "\n\n" );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}