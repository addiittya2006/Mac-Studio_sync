package se.popcorn_time.base.updater;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class UpdaterResultReceiver extends ResultReceiver {

    private UpdaterReceiver mReceiver;

    public UpdaterResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(UpdaterReceiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}