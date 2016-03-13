package se.popcorn_time.base.model.video.info;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import se.popcorn_time.base.providers.subtitles.SubtitlesProvider;
import se.popcorn_time.base.providers.video.info.VideoInfoProvider;

public abstract class VideoInfo implements Parcelable {

    private String videoType;
    public String title;
    public String year;
    public float rating;
    public String imdb;
    public String actors;
    public String trailer;
    public String description;
    public String posterMediumUrl;
    public String posterBigUrl;
    protected int torrentPosition = -1;

    public VideoInfo(String videoType) {
        this.videoType = videoType;
    }

    protected VideoInfo(Parcel parcel) {
        videoType = parcel.readString();
        title = parcel.readString();
        year = parcel.readString();
        rating = parcel.readFloat();
        imdb = parcel.readString();
        actors = parcel.readString();
        trailer = parcel.readString();
        description = parcel.readString();
        posterMediumUrl = parcel.readString();
        posterBigUrl = parcel.readString();
        torrentPosition = parcel.readInt();
    }

    public abstract ArrayList<VideoInfoProvider> getVideoInfoProviders();

    public abstract ArrayList<SubtitlesProvider> getSubtitlesProviders();

    public abstract ArrayList<Torrent> getTorrents();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(videoType);
        dest.writeString(title);
        dest.writeString(year);
        dest.writeFloat(rating);
        dest.writeString(imdb);
        dest.writeString(actors);
        dest.writeString(trailer);
        dest.writeString(description);
        dest.writeString(posterMediumUrl);
        dest.writeString(posterBigUrl);
        dest.writeInt(torrentPosition);
    }

    public final String getVideoType() {
        return videoType;
    }

    public final int getTorrentPosition() {
        return torrentPosition;
    }

    public final void setTorrentPosition(int torrentPosition) {
        if (getTorrents() != null && torrentPosition >= 0 && torrentPosition < getTorrents().size()) {
            this.torrentPosition = torrentPosition;
        }
    }

    @Nullable
    public final Torrent getCurrentTorrent() {
        if (getTorrents() != null && torrentPosition >= 0 && torrentPosition < getTorrents().size()) {
            return getTorrents().get(torrentPosition);
        }
        return null;
    }
}