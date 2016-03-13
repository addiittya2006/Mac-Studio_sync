package se.popcorn_time.mobile.ui.base;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.updater.Updater;
import se.popcorn_time.mobile.ui.locale.LocaleActivity;

public abstract class PopcornBaseActivity extends LocaleActivity {

    private Toolbar toolbar;
    private TextView title;
    private FrameLayout content;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) toolbar.findViewById(R.id.title);
        content = (FrameLayout) findViewById(R.id.content);
        logo = (ImageView) findViewById(R.id.logo);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        title.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Updater.getInstance().setCurrentActivity(PopcornBaseActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Updater.getInstance().setCurrentActivity(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Toolbar getPopcornToolbar() {
        return toolbar;
    }

    public TextView getPopcornTitle() {
        return title;
    }

    public ImageView getPopcornLogoView() {
        return logo;
    }

    public View setPopcornContentView(@LayoutRes int resource) {
        View view = LayoutInflater.from(PopcornBaseActivity.this).inflate(resource, null, false);
        content.addView(view);
        return view;
    }
}