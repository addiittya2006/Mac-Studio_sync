package se.popcorn_time.base.database.tables;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import se.popcorn_time.base.database.DBProvider;
import se.popcorn_time.base.model.video.category.Anime;
import se.popcorn_time.base.model.video.category.Cinema;
import se.popcorn_time.base.model.video.info.AnimeMoviesInfo;
import se.popcorn_time.base.model.video.info.AnimeTvShowsInfo;
import se.popcorn_time.base.model.video.info.CinemaMoviesInfo;
import se.popcorn_time.base.model.video.info.CinemaTvShowsInfo;
import se.popcorn_time.base.model.video.info.MoviesInfo;
import se.popcorn_time.base.model.video.info.Torrent;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.utils.JSONHelper;
import se.popcorn_time.base.utils.Logger;

public class Favorites implements BaseColumns {

    public static final String _TYPE = "_type";
    public static final String _TITLE = "_title";
    public static final String _YEAR = "_year";
    public static final String _RATING = "_rating";
    public static final String _IMDB = "_imdb";
    public static final String _ACTORS = "_actors";
    public static final String _TRAILER = "_trailer";
    public static final String _DESCRIPTION = "_description";
    public static final String _POSTER_MEDIUM_URL = "_poster_medium_url";
    public static final String _POSTER_BIG_URL = "_poster_big_url";
    public static final String _TORRENTS_INFO = "_torrents_info";

    private static final String NAME = Tables.FAVORITES;
    public static final Uri CONTENT_URI = DBProvider.BASE_CONTENT_URI.buildUpon().appendPath(NAME).build();
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.popcorn." + NAME;
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.popcorn." + NAME;

    public static final String QUERY_CREATE = "CREATE TABLE " + NAME + " ("
            + _ID + " INTEGER PRIMARY KEY, "
            + _TYPE + " TEXT, "
            + _TITLE + " TEXT, "
            + _YEAR + " TEXT, "
            + _RATING + " REAL, "
            + _IMDB + " TEXT, "
            + _ACTORS + " TEXT, "
            + _TRAILER + " TEXT, "
            + _DESCRIPTION + " TEXT, "
            + _POSTER_MEDIUM_URL + " TEXT, "
            + _POSTER_BIG_URL + " TEXT, "
            + _TORRENTS_INFO + " TEXT, "
            + "UNIQUE (" + _IMDB + ") ON CONFLICT REPLACE)";

    public static Uri buildUri(long id) {
        return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
    }

    public static Cursor query(Context context, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return context.getContentResolver().query(CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public static Uri insert(Context context, VideoInfo info) {
        return context.getContentResolver().insert(CONTENT_URI, buildValues(info));
    }

    public static int update(Context context, VideoInfo info) {
        return context.getContentResolver().update(CONTENT_URI, buildValues(info), _IMDB + "=\"" + info.imdb + "\"", null);
    }

    public static int delete(Context context, VideoInfo info) {
        return context.getContentResolver().delete(CONTENT_URI, _IMDB + "=\"" + info.imdb + "\"", null);
    }

    private static ContentValues buildValues(VideoInfo videoInfo) {
        ContentValues values = new ContentValues();
        String type = videoInfo.getVideoType();
        values.put(_TYPE, type);
        values.put(_TITLE, videoInfo.title);
        values.put(_YEAR, videoInfo.year);
        values.put(_RATING, videoInfo.rating);
        values.put(_IMDB, videoInfo.imdb);
        values.put(_ACTORS, videoInfo.actors);
        values.put(_TRAILER, videoInfo.trailer);
        values.put(_DESCRIPTION, videoInfo.description);
        values.put(_POSTER_MEDIUM_URL, videoInfo.posterMediumUrl);
        values.put(_POSTER_BIG_URL, videoInfo.posterBigUrl);
        switch (type) {
            case Cinema.TYPE_MOVIES:
            case Anime.TYPE_MOVIES:
                try {
                    values.put(_TORRENTS_INFO, toJsonArray(videoInfo.getTorrents()).toString());
                } catch (JSONException e) {
                    Logger.error("Favorites<buildValues>: Error", e);
                }
                break;
        }
        return values;
    }

    @Nullable
    public static VideoInfo create(Cursor cursor) throws JSONException, IllegalArgumentException {
        String type = cursor.getString(cursor.getColumnIndexOrThrow(_TYPE));
        switch (type) {
            case Cinema.TYPE_MOVIES:
                return createCinemaMovies(cursor);
            case Cinema.TYPE_TV_SHOWS:
                return createCinemaTvShows(cursor);
            case Anime.TYPE_MOVIES:
                return createAnimeMovies(cursor);
            case Anime.TYPE_TV_SHOWS:
                return createAnimeTvShows(cursor);
            default:
                Logger.error("Favorites: wrong video type - " + type);
                return null;
        }
    }

    @NonNull
    private static CinemaMoviesInfo createCinemaMovies(Cursor cursor) throws JSONException {
        CinemaMoviesInfo info = new CinemaMoviesInfo();
        populateVideoInfo(info, cursor);
        String json = cursor.getString(cursor.getColumnIndexOrThrow(_TORRENTS_INFO));
        if (!TextUtils.isEmpty(json)) {
            populateTorrents(info, new JSONArray(json));
        }
        return info;
    }

    @NonNull
    private static CinemaTvShowsInfo createCinemaTvShows(Cursor cursor) throws JSONException {
        CinemaTvShowsInfo info = new CinemaTvShowsInfo();
        populateVideoInfo(info, cursor);
        return info;
    }

    @NonNull
    private static AnimeMoviesInfo createAnimeMovies(Cursor cursor) throws JSONException {
        AnimeMoviesInfo info = new AnimeMoviesInfo();
        populateVideoInfo(info, cursor);
        String json = cursor.getString(cursor.getColumnIndexOrThrow(_TORRENTS_INFO));
        if (!TextUtils.isEmpty(json)) {
            populateTorrents(info, new JSONArray(json));
        }
        return info;
    }

    @NonNull
    private static AnimeTvShowsInfo createAnimeTvShows(Cursor cursor) throws JSONException {
        AnimeTvShowsInfo info = new AnimeTvShowsInfo();
        populateVideoInfo(info, cursor);
        return info;
    }

    private static void populateVideoInfo(VideoInfo videoInfo, Cursor cursor) {
        videoInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(_TITLE));
        videoInfo.year = cursor.getString(cursor.getColumnIndexOrThrow(_YEAR));
        videoInfo.rating = cursor.getFloat(cursor.getColumnIndexOrThrow(_RATING));
        videoInfo.imdb = cursor.getString(cursor.getColumnIndexOrThrow(_IMDB));
        videoInfo.actors = cursor.getString(cursor.getColumnIndexOrThrow(_ACTORS));
        videoInfo.trailer = cursor.getString(cursor.getColumnIndexOrThrow(_TRAILER));
        videoInfo.description = cursor.getString(cursor.getColumnIndexOrThrow(_DESCRIPTION));
        videoInfo.posterMediumUrl = cursor.getString(cursor.getColumnIndexOrThrow(_POSTER_MEDIUM_URL));
        videoInfo.posterBigUrl = cursor.getString(cursor.getColumnIndexOrThrow(_POSTER_BIG_URL));
    }

    private static void populateTorrents(MoviesInfo info, JSONArray jsonTorrents) throws JSONException {
        for (int i = 0; i < jsonTorrents.length(); i++) {
            JSONObject jsonTorrent = jsonTorrents.getJSONObject(i);
            Torrent torrent = new Torrent();
            torrent.url = JSONHelper.getString(jsonTorrent, "torrent_url", null);
            torrent.magnet = JSONHelper.getString(jsonTorrent, "torrent_magnet", null);
            torrent.seeds = JSONHelper.getInt(jsonTorrent, "torrent_seeds", 0);
            torrent.peers = JSONHelper.getInt(jsonTorrent, "torrent_peers", 0);
            torrent.file = JSONHelper.getString(jsonTorrent, "file", null);
            torrent.quality = JSONHelper.getString(jsonTorrent, "quality", null);
            torrent.size = JSONHelper.getLong(jsonTorrent, "size_bytes", 0);
            info.torrents.add(torrent);
        }
        info.setTorrentPosition(0);
    }

    @NonNull
    private static JSONArray toJsonArray(List<Torrent> torrents) throws JSONException {
        JSONArray jsonTorrents = new JSONArray();
        for (Torrent torrent : torrents) {
            JSONObject jsonTorrent = new JSONObject();
            jsonTorrent.put("torrent_url", torrent.url);
            jsonTorrent.put("torrent_magnet", torrent.magnet);
            jsonTorrent.put("torrent_seeds", torrent.seeds);
            jsonTorrent.put("torrent_peers", torrent.peers);
            jsonTorrent.put("file", torrent.file);
            jsonTorrent.put("quality", torrent.quality);
            jsonTorrent.put("size_bytes", torrent.size);
            jsonTorrents.put(jsonTorrent);
        }
        return jsonTorrents;
    }
}