package se.popcorn_time.base.subtitles;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.player.subtitles.SubtitlesUtils;

import java.util.ArrayList;

public class Subtitles implements Parcelable {

    public static final int WITHOUT_POSITION = 0;
    public static final int CUSTOM_POSITION = 1;
    public static final int START_INDEX = 2;

    private ArrayList<String> languages = new ArrayList<>();
    private ArrayList<String> urls = new ArrayList<>();
    private int position = WITHOUT_POSITION;

    public Subtitles() {
        languages.add(WITHOUT_POSITION, SubtitlesUtils.WITHOUT_SUBTITLES);
        languages.add(CUSTOM_POSITION, SubtitlesUtils.CUSTOM_SUBTITLES);
        urls.add(WITHOUT_POSITION, null);
        urls.add(CUSTOM_POSITION, null);
    }

    private Subtitles(Parcel parcel) {
        parcel.readStringList(languages);
        parcel.readStringList(urls);
        position = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(languages);
        dest.writeStringList(urls);
        dest.writeInt(position);
    }

    public void setCustom(String url) {
        urls.set(CUSTOM_POSITION, url);
    }

    public boolean add(String language, String url) {
        if (languages.contains(language)) {
            return false;
        }
        languages.add(language);
        urls.add(url);
        return true;
    }

    public ArrayList<String> getLanguages() {
        return languages;
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Nullable
    public String getCurrentUrl() {
        if (urls != null && urls.size() > position) {
            return urls.get(position);
        }
        return null;
    }

    public static final Creator<Subtitles> CREATOR = new Creator<Subtitles>() {
        @Override
        public Subtitles createFromParcel(Parcel source) {
            return new Subtitles(source);
        }

        @Override
        public Subtitles[] newArray(int size) {
            return new Subtitles[size];
        }
    };
}