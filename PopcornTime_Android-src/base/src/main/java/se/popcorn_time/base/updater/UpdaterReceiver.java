package se.popcorn_time.base.updater;

import android.os.Bundle;

public interface UpdaterReceiver {
    void onReceiveResult(int resultCode, Bundle resultData);
}