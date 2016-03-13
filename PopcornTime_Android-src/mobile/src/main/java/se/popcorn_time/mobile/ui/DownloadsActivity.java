package se.popcorn_time.mobile.ui;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import dp.ws.popcorntime.R;
import se.popcorn_time.base.database.tables.Downloads;
import se.popcorn_time.base.model.DownloadInfo;
import se.popcorn_time.base.torrent.client.DownloadsClient;
import se.popcorn_time.mobile.ui.adapter.DownloadsAdapter;
import se.popcorn_time.mobile.ui.base.PopcornBaseActivity;
import se.popcorn_time.mobile.ui.dialog.OptionDialog;

public class DownloadsActivity extends PopcornBaseActivity implements LoaderCallbacks<Cursor> {

    public static final String VIDEO_URL = "video-url";

    private boolean init;
    private DownloadsClient downloadsClient;
    private DownloadsAdapter mDownloadsAdapter;
    private ListView downloadsList;
    private OptionDialog optionDialog = new OptionDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Popcorn_Classic);
        super.onCreate(savedInstanceState);
        init = getIntent().hasExtra(VIDEO_URL);
        downloadsClient = new DownloadsClient(DownloadsActivity.this);

        // Toolbar
        getPopcornLogoView().setVisibility(View.GONE);
        getPopcornTitle().setVisibility(View.VISIBLE);

        // Content
        View content = setPopcornContentView(R.layout.activity_downloads);
        mDownloadsAdapter = new DownloadsAdapter(DownloadsActivity.this, downloadsClient);
        downloadsList = (ListView) content.findViewById(R.id.downloads_list);
        downloadsList.setAdapter(mDownloadsAdapter);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, DownloadsActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        downloadsClient.bind();
    }

    @Override
    protected void onStop() {
        super.onStop();
        downloadsClient.unbind();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.downloads, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.downloads_resume_all:
                downloadsClient.downloadsResumeAll();
                return true;
            case R.id.downloads_pause_all:
                downloadsClient.downloadsPauseAll();
                return true;
            case R.id.downloads_remove_all:
                showRemoveAllDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updateLocaleText() {
        super.updateLocaleText();
        getPopcornTitle().setText(R.string.downloads);
        mDownloadsAdapter.updateLocaleText();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(DownloadsActivity.this, Downloads.CONTENT_URI, null, null, null, Downloads._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mDownloadsAdapter.swapCursor(cursor);
        if (init) {
            init = false;
            int position = 0;
            String url = getIntent().getStringExtra(VIDEO_URL);
            DownloadInfo downloadInfo = new DownloadInfo();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                try {
                    downloadInfo.populate(cursor);
                    if (url.equals(downloadInfo.torrentUrl)) {
                        position = i;
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (0 != position) {
                setSelection(position);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDownloadsAdapter.swapCursor(null);
    }

    private void setSelection(final int position) {
        downloadsList.post(new Runnable() {
            @Override
            public void run() {
                downloadsList.setSelection(position);
            }
        });
    }

    private void showRemoveAllDialog() {
        if (!optionDialog.isAdded()) {
            optionDialog.setListener(removeAllListener);
            optionDialog.setArguments(OptionDialog.createArguments(getString(R.string.remove_all), getString(R.string.downloads_remove_msg)));
            optionDialog.show(getSupportFragmentManager(), "downloads_remove_all_dialog");
        }
    }

    private OptionDialog.SimpleOptionListener removeAllListener = new OptionDialog.SimpleOptionListener() {
        @Override
        public boolean positiveShow() {
            return true;
        }

        @Override
        public String positiveButtonText() {
            return getString(android.R.string.ok);
        }
        
        @Override
        public void positiveAction() {
            downloadsClient.downloadsRemoveAll();
        }

        @Override
        public boolean neutralShow() {
            return true;
        }

        @Override
        public String neutralButtonText() {
            return getString(android.R.string.cancel);
        }
    };

    public static void start(Context context) {
        context.startActivity(new Intent(context, DownloadsActivity.class));
    }
}