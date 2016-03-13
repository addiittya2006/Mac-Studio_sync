package se.popcorn_time.base.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class BaseLoader<Result> extends AsyncTaskLoader<Result> {

    private Result mResult;

    public BaseLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(Result result) {
        if (isReset()) {
            if (result != null) {
                onReleaseResources(result);
            }
        }
        if (mResult != null) {
            onReleaseResources(mResult);
        }
        mResult = result;
        if (isStarted()) {
            super.deliverResult(result);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(Result result) {
        super.onCanceled(result);
        onReleaseResources(result);
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if (mResult != null) {
            onReleaseResources(mResult);
            mResult = null;
        }
    }

    protected void onReleaseResources(Result result) {

    }
}