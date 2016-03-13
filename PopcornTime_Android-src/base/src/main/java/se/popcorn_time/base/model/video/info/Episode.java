package se.popcorn_time.base.model.video.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Episode implements Parcelable {

    private int number;
    public String title;
    public String description;
    public ArrayList<Torrent> torrents = new ArrayList<>();

    public Episode(int number) {
        this.number = number;
    }

    private Episode(Parcel parcel) {
        number = parcel.readInt();
        title = parcel.readString();
        description = parcel.readString();
        parcel.readTypedList(torrents, Torrent.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(number);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeTypedList(torrents);
    }

    public int getNumber() {
        return number;
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel source) {
            return new Episode(source);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };
}