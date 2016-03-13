package se.popcorn_time.base.updater;

import android.os.Parcel;

import se.popcorn_time.base.providers.HttpProvider;

public abstract class UpdaterProvider extends HttpProvider<UpdaterInfo> {

    public UpdaterProvider() {
    }

    protected UpdaterProvider(Parcel source) {
        super(source);
    }

    @Override
    public UpdaterInfo create() {
        return new UpdaterInfo();
    }
}