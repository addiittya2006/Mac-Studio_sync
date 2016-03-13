package se.popcorn_time.base.model.video.info;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Season implements Parcelable {

    private int number;
    public ArrayList<Episode> episodes = new ArrayList<>();

    public Season(int number) {
        this.number = number;
    }

    private Season(Parcel parcel) {
        number = parcel.readInt();
        parcel.readTypedList(episodes, Episode.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(number);
        dest.writeTypedList(episodes);
    }

    public int getNumber() {
        return number;
    }

    public static final Creator<Season> CREATOR = new Creator<Season>() {
        @Override
        public Season createFromParcel(Parcel source) {
            return new Season(source);
        }

        @Override
        public Season[] newArray(int size) {
            return new Season[size];
        }
    };
}