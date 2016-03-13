package se.popcorn_time.base.model.video.info;

import android.os.Parcel;
import android.os.Parcelable;

public class Torrent implements Parcelable {

    public String url;
    public String magnet;
    public int seeds;
    public int peers;
    public String file;
    public String quality;
    public long size;

    public Torrent() {
    }

    private Torrent(Parcel parcel) {
        url = parcel.readString();
        magnet = parcel.readString();
        seeds = parcel.readInt();
        peers = parcel.readInt();
        file = parcel.readString();
        quality = parcel.readString();
        size = parcel.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(magnet);
        dest.writeInt(seeds);
        dest.writeInt(peers);
        dest.writeString(file);
        dest.writeString(quality);
        dest.writeLong(size);
    }

    public static final Creator<Torrent> CREATOR = new Creator<Torrent>() {
        @Override
        public Torrent createFromParcel(Parcel source) {
            return new Torrent(source);
        }

        @Override
        public Torrent[] newArray(int size) {
            return new Torrent[size];
        }
    };
}