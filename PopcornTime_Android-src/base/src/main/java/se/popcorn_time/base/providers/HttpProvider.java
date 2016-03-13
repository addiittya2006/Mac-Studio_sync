package se.popcorn_time.base.providers;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class HttpProvider<Data> extends BaseProvider<String, String, Data, String> implements Parcelable {

    public HttpProvider() {
    }

    protected HttpProvider(Parcel source) {
        params = source.createStringArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(params);
    }
}