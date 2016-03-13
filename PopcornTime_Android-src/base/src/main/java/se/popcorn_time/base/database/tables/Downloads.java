package se.popcorn_time.base.database.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import se.popcorn_time.base.database.DBProvider;
import se.popcorn_time.base.model.DownloadInfo;

public class Downloads implements BaseColumns {

    public static final String _TYPE = "_type";
    public static final String _IMDB = "_imdb";
    public static final String _TORRENT_URL = "_torrent_url";
    public static final String _TORRENT_MAGNET = "_torrent_magnet";
    public static final String _FILE_NAME = "_file_name";
    public static final String _POSTER_URL = "_poster_url";
    public static final String _TITLE = "_title";
    public static final String _DIRECTORY = "_directory";
    public static final String _TORRENT_FILE_PATH = "_torrent_file_path";
    public static final String _STATE = "_state";
    public static final String _SIZE = "_size";
    public static final String _SEASON = "_season";
    public static final String _EPISODE = "_episode";

    private static final String NAME = Tables.DOWNLOADS;
    public static final Uri CONTENT_URI = DBProvider.BASE_CONTENT_URI.buildUpon().appendPath(NAME).build();
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.popcorn." + NAME;
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.popcorn." + NAME;

    public static final String QUERY_CREATE = "CREATE TABLE " + NAME + " ("
            + _ID + " INTEGER PRIMARY KEY, "
            + _TYPE + " TEXT, "
            + _IMDB + " TEXT, "
            + _TORRENT_URL + " TEXT, "
            + _TORRENT_MAGNET + " TEXT, "
            + _FILE_NAME + " TEXT, "
            + _POSTER_URL + " TEXT, "
            + _TITLE + " TEXT, "
            + _DIRECTORY + " TEXT, "
            + _TORRENT_FILE_PATH + " TEXT, "
            + _STATE + " INTEGER, "
            + _SIZE + " INTEGER, "
            + _SEASON + " INTEGER, "
            + _EPISODE + " INTEGER"
            + ")";

    public static Uri buildUri(long id) {
        return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
    }

    public static Cursor query(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return context.getContentResolver().query(CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public static Uri insert(Context context, DownloadInfo info) {
        return context.getContentResolver().insert(CONTENT_URI, buildValues(info));
    }

    public static int update(Context context, DownloadInfo info) {
        return context.getContentResolver().update(buildUri(info.id), buildValues(info), null, null);
    }

    public static int delete(Context context, long id) {
        return context.getContentResolver().delete(buildUri(id), null, null);
    }

    private static ContentValues buildValues(DownloadInfo info) {
        ContentValues values = new ContentValues();
        values.put(_TYPE, info.type);
        values.put(_IMDB, info.imdb);
        values.put(_TORRENT_URL, info.torrentUrl);
        values.put(_TORRENT_MAGNET, info.torrentMagnet);
        values.put(_FILE_NAME, info.fileName);
        values.put(_POSTER_URL, info.posterUrl);
        values.put(_TITLE, info.title);
        values.put(_DIRECTORY, info.directory.getAbsolutePath());
        values.put(_TORRENT_FILE_PATH, info.torrentFilePath);
        values.put(_STATE, info.state);
        values.put(_SIZE, info.size);
        values.put(_SEASON, info.season);
        values.put(_EPISODE, info.episode);
        return values;
    }
}