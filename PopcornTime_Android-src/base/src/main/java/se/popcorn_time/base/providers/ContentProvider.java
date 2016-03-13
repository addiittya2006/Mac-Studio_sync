package se.popcorn_time.base.providers;

import android.os.Parcel;

public abstract class ContentProvider<Data> extends HttpProvider<Data> {

    protected int page = 1;

    public ContentProvider() {

    }

    protected ContentProvider(Parcel source) {
        super(source);
        page = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(page);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void incrementPage() {
        page++;
    }

    public void decrementPage() {
        page--;
    }
}