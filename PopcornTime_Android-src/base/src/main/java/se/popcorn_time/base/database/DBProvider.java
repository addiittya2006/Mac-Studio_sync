package se.popcorn_time.base.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import se.popcorn_time.base.database.tables.Downloads;
import se.popcorn_time.base.database.tables.Favorites;
import se.popcorn_time.base.database.tables.Tables;

public class DBProvider extends ContentProvider {

    public static final String CONTENT_AUTHORITY = "dp.ws.popcorntime";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final int FAVORITES = 100;
    private static final int FAVORITES_ID = 101;
    private static final int DOWNLOADS = 200;
    private static final int DOWNLOADS_ID = 201;

    private DBHelper mHelper;
    private UriMatcher mUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(CONTENT_AUTHORITY, Tables.FAVORITES, FAVORITES);
        matcher.addURI(CONTENT_AUTHORITY, Tables.FAVORITES + "/#", FAVORITES_ID);
        matcher.addURI(CONTENT_AUTHORITY, Tables.DOWNLOADS, DOWNLOADS);
        matcher.addURI(CONTENT_AUTHORITY, Tables.DOWNLOADS + "/#", DOWNLOADS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                return Favorites.CONTENT_TYPE;
            case FAVORITES_ID:
                return Favorites.CONTENT_ITEM_TYPE;
            case DOWNLOADS:
                return Downloads.CONTENT_TYPE;
            case DOWNLOADS_ID:
                return Downloads.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = null;

        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                cursor = db.query(Tables.FAVORITES, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVORITES_ID:
                cursor = db.query(Tables.FAVORITES, projection, selectionWithId(selection, uri.getLastPathSegment()), selectionArgs, null, null, sortOrder);
                break;
            case DOWNLOADS:
                cursor = db.query(Tables.DOWNLOADS, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case DOWNLOADS_ID:
                cursor = db.query(Tables.DOWNLOADS, projection, selectionWithId(selection, uri.getLastPathSegment()), selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Wrong uri: " + uri);
        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        Uri insertUri = null;

        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                long favoriteId = db.insert(Tables.FAVORITES, null, values);
                insertUri = Favorites.buildUri(favoriteId);
                break;
            case DOWNLOADS:
                long downloadId = db.insert(Tables.DOWNLOADS, null, values);
                insertUri = Downloads.buildUri(downloadId);
                break;
            default:
                throw new UnsupportedOperationException("Wrong uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return insertUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        int itemDeletedCount = 0;

        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                itemDeletedCount = db.delete(Tables.FAVORITES, selection, selectionArgs);
                break;
            case FAVORITES_ID:
                itemDeletedCount = db.delete(Tables.FAVORITES, selectionWithId(selection, uri.getLastPathSegment()), selectionArgs);
                break;
            case DOWNLOADS:
                itemDeletedCount = db.delete(Tables.DOWNLOADS, selection, selectionArgs);
                break;
            case DOWNLOADS_ID:
                itemDeletedCount = db.delete(Tables.DOWNLOADS, selectionWithId(selection, uri.getLastPathSegment()), selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Wrong uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return itemDeletedCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mHelper.getWritableDatabase();
        int itemUpdatedCount = 0;

        switch (mUriMatcher.match(uri)) {
            case FAVORITES:
                itemUpdatedCount = db.update(Tables.FAVORITES, values, selection, selectionArgs);
                break;
            case FAVORITES_ID:
                itemUpdatedCount = db.update(Tables.FAVORITES, values, selectionWithId(selection, uri.getLastPathSegment()), selectionArgs);
                break;
            case DOWNLOADS:
                itemUpdatedCount = db.update(Tables.DOWNLOADS, values, selection, selectionArgs);
                break;
            case DOWNLOADS_ID:
                itemUpdatedCount = db.update(Tables.DOWNLOADS, values, selectionWithId(selection, uri.getLastPathSegment()), selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Wrong uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return itemUpdatedCount;
    }

    private String selectionWithId(String selection, String id) {
        if (TextUtils.isEmpty(selection)) {
            selection = BaseColumns._ID + "=" + id;
        } else {
            selection += " AND " + BaseColumns._ID + "=" + id;
        }

        return selection;
    }

}