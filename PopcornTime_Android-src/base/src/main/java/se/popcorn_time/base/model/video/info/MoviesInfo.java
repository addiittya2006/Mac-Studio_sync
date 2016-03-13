package se.popcorn_time.base.model.video.info;

import android.os.Parcel;

import java.util.ArrayList;

public abstract class MoviesInfo extends VideoInfo {

    public ArrayList<Torrent> torrents = new ArrayList<>();

    public MoviesInfo(String videoType) {
        super(videoType);
    }

    protected MoviesInfo(Parcel parcel) {
        super(parcel);
        parcel.readTypedList(torrents, Torrent.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(torrents);
    }

    @Override
    public ArrayList<Torrent> getTorrents() {
        return torrents;
    }
}