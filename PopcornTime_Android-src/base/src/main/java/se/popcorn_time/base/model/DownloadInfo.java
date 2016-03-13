package se.popcorn_time.base.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;

import se.popcorn_time.base.database.tables.Downloads;
import se.popcorn_time.base.model.video.category.Anime;
import se.popcorn_time.base.model.video.category.Cinema;
import se.popcorn_time.base.model.video.info.CinemaMoviesInfo;
import se.popcorn_time.base.model.video.info.CinemaTvShowsInfo;
import se.popcorn_time.base.providers.subtitles.SubtitlesProvider;

public class DownloadInfo implements Parcelable {

    public long id;
    public String type;
    public String imdb;
    public String torrentUrl;
    public String torrentMagnet;
    public String fileName;
    public String posterUrl;
    public String title;
    public File directory;
    public String torrentFilePath;
    public int state;
    public long size;
    public int season;
    public int episode;

    public DownloadInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    private DownloadInfo(Parcel parcel) {
        id = parcel.readLong();
        type = parcel.readString();
        imdb = parcel.readString();
        torrentUrl = parcel.readString();
        torrentMagnet = parcel.readString();
        fileName = parcel.readString();
        posterUrl = parcel.readString();
        title = parcel.readString();
        directory = new File(parcel.readString());
        torrentFilePath = parcel.readString();
        state = parcel.readInt();
        size = parcel.readLong();
        season = parcel.readInt();
        episode = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(type);
        parcel.writeString(imdb);
        parcel.writeString(torrentUrl);
        parcel.writeString(torrentMagnet);
        parcel.writeString(fileName);
        parcel.writeString(posterUrl);
        parcel.writeString(title);
        parcel.writeString(directory.getAbsolutePath());
        parcel.writeString(torrentFilePath);
        parcel.writeInt(state);
        parcel.writeLong(size);
        parcel.writeInt(season);
        parcel.writeInt(episode);
    }

    public void populate(Cursor cursor) throws IllegalArgumentException {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(Downloads._ID));
        type = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TYPE));
        imdb = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._IMDB));
        torrentUrl = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TORRENT_URL));
        torrentMagnet = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TORRENT_MAGNET));
        fileName = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._FILE_NAME));
        posterUrl = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._POSTER_URL));
        title = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TITLE));
        directory = new File(cursor.getString(cursor.getColumnIndexOrThrow(Downloads._DIRECTORY)));
        torrentFilePath = cursor.getString(cursor.getColumnIndexOrThrow(Downloads._TORRENT_FILE_PATH));
        state = cursor.getInt(cursor.getColumnIndexOrThrow(Downloads._STATE));
        size = cursor.getLong(cursor.getColumnIndexOrThrow(Downloads._SIZE));
        season = cursor.getInt(cursor.getColumnIndexOrThrow(Downloads._SEASON));
        episode = cursor.getInt(cursor.getColumnIndexOrThrow(Downloads._EPISODE));
    }

    public ArrayList<SubtitlesProvider> getSubtitlesProvider() {
        switch (type) {
            case Cinema.TYPE_MOVIES:
                return CinemaMoviesInfo.createSubtitlesProviders(imdb);
            case Cinema.TYPE_TV_SHOWS:
                return CinemaTvShowsInfo.createSubtitlesProviders(imdb, season, episode);
            case Anime.TYPE_MOVIES:
                return null;
            case Anime.TYPE_TV_SHOWS:
                return null;
            default:
                throw new IllegalArgumentException("Wrong video type: " + type);
        }
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {

        public DownloadInfo createFromParcel(Parcel in) {
            return new DownloadInfo(in);
        }

        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };
}