package se.popcorn_time.base.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import se.popcorn_time.base.providers.subtitles.SubtitlesProvider;
import se.popcorn_time.base.subtitles.Subtitles;

public class WatchInfo implements Parcelable {

    public boolean isDownloads = false;
    public String type;
    public String watchDir;
    public String torrentFilePath;
    public String torrentUrl;
    public String torrentMagnet;
    public String fileName;
    public ArrayList<SubtitlesProvider> subtitlesProviders;
    public Subtitles subtitles;

    public WatchInfo() {

    }

    public WatchInfo(DownloadInfo downloadInfo, Subtitles subtitles) {
        if (downloadInfo != null) {
            isDownloads = true;
            type = downloadInfo.type;
            watchDir = downloadInfo.directory.getAbsolutePath();
            torrentFilePath = downloadInfo.torrentFilePath;
            torrentUrl = downloadInfo.torrentUrl;
            torrentMagnet = downloadInfo.torrentMagnet;
            fileName = downloadInfo.fileName;
            subtitlesProviders = downloadInfo.getSubtitlesProvider();
        }
        this.subtitles = subtitles;
    }

    private WatchInfo(Parcel parcel) {
        isDownloads = parcel.readByte() == 1;
        type = parcel.readString();
        watchDir = parcel.readString();
        torrentFilePath = parcel.readString();
        torrentUrl = parcel.readString();
        torrentMagnet = parcel.readString();
        fileName = parcel.readString();
        parcel.readList(subtitlesProviders, ArrayList.class.getClassLoader());
        subtitles = parcel.readParcelable(Subtitles.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByte((byte) (isDownloads ? 1 : 0));
        parcel.writeString(type);
        parcel.writeString(watchDir);
        parcel.writeString(torrentFilePath);
        parcel.writeString(torrentUrl);
        parcel.writeString(torrentMagnet);
        parcel.writeString(fileName);
        parcel.writeList(subtitlesProviders);
        parcel.writeParcelable(subtitles, flags);
    }

    public static final Creator<WatchInfo> CREATOR = new Creator<WatchInfo>() {
        @Override
        public WatchInfo createFromParcel(Parcel source) {
            return new WatchInfo(source);
        }

        @Override
        public WatchInfo[] newArray(int size) {
            return new WatchInfo[size];
        }
    };
}