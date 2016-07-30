package com.zackliston.fulltextsearch.search_database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Zack Liston on 5/27/15.
 */
public class SearchDatabase extends SQLiteOpenHelper {

    class SearchReturn {
        private List<SearchResult> results;
        private List<String> suggestions;

        public List<SearchResult> getResults() {
            return results;
        }
        public List<String> getSuggestions() {
            return suggestions;
        }
    }

    //region Constants
    private static final int    DATABASE_VERSION        = 1;

    //region Table Names
    static final String INDEX_TABLE_NAME        = "searchindex";
    static final String METADATA_TABLE_NAME     = "searchmetadata";
    //endregion

    //region Field Names
    static final String MODULE_ID_KEY           = "moduleid";
    static final String FILE_ID_KEY             = "fileid";
    static final String LANGUAGE_KEY            = "language";
    static final String BOOST_KEY               = "boost";
    static final String WEIGHT_0_KEY            = "weight0";
    static final String WEIGHT_1_KEY            = "weight1";
    static final String WEIGHT_2_KEY            = "weight2";
    static final String WEIGHT_3_KEY            = "weight3";
    static final String WEIGHT_4_KEY            = "weight4";

    static final String[] INDEX_TABLE_COLUMNS = {MODULE_ID_KEY, FILE_ID_KEY, LANGUAGE_KEY, BOOST_KEY, WEIGHT_0_KEY, WEIGHT_1_KEY, WEIGHT_2_KEY, WEIGHT_3_KEY, WEIGHT_4_KEY};

    static final String TITLE_KEY               = "title";
    static final String SUBTITLE_KEY            = "subtitle";
    static final String URI_KEY                 = "uri";
    static final String TYPE_KEY                = "type";
    static final String IMAGE_URI_KEY           = "imageuri";

    static final String[] METADATA_TABLE_COLUMNS = {MODULE_ID_KEY, FILE_ID_KEY, TITLE_KEY, SUBTITLE_KEY, URI_KEY, TYPE_KEY, IMAGE_URI_KEY};
    //endregion
    //endregion

    //region Initialize
    SearchDatabase(Context context, String databaseName)
    {
        super(context, databaseName, null, DATABASE_VERSION);
    }
    //endregion

    //region Database Methods
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //initializeDatabaseTable(db);
        //issueAutomergeCommand(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        this.onCreate(db);
    }
    //endregion

    //region Setup
    private void initializeDatabaseTable(SQLiteDatabase db) {
        // Create statement
        //todo crashes if we use if not exists. Figure out if not using it is ok
        final String INDEX_TABLE_CREATE_COMMAND = "CREATE VIRTUAL TABLE " + INDEX_TABLE_NAME + " USING FTS3 ( " +
                MODULE_ID_KEY + " TEXT NOT NULL, " +
                FILE_ID_KEY + " TEXT NOT NULL, " +
                LANGUAGE_KEY + " TEXT NOT NULL, " +
                BOOST_KEY + " FLOAT NOT NULL, " +
                WEIGHT_0_KEY + " TEXT, " +
                WEIGHT_1_KEY + " TEXT, " +
                WEIGHT_2_KEY + " TEXT, " +
                WEIGHT_3_KEY + " TEXT, " +
                WEIGHT_4_KEY + " TEXT, " +
                "PRIMARY KEY (" + MODULE_ID_KEY + ", " + FILE_ID_KEY + ")); ";

        final String METADATA_TABLE_CREATE_COMMAND = "CREATE TABLE IF NOT EXISTS " + METADATA_TABLE_NAME + " (" +
                MODULE_ID_KEY + " TEXT NOT NULL, " +
                FILE_ID_KEY + " TEXT NOT NULL, " +
                TITLE_KEY + " TEXT, " +
                SUBTITLE_KEY + " TEXT, " +
                URI_KEY + " TEXT, " +
                TYPE_KEY + " TEXT, " +
                IMAGE_URI_KEY + " TEXT, " +
                "PRIMARY KEY (" + MODULE_ID_KEY + ", " + FILE_ID_KEY + "));";

        db.execSQL(INDEX_TABLE_CREATE_COMMAND);
        db.execSQL(METADATA_TABLE_CREATE_COMMAND);
    }

    private void issueAutomergeCommand(SQLiteDatabase database) {
        final int AUTOMERGE_NUMBER = 2;
        final String COMMAND = "INSERT INTO " + INDEX_TABLE_NAME + "("+INDEX_TABLE_NAME +") VALUES('automerge=" + AUTOMERGE_NUMBER + "');";
        database.execSQL(COMMAND);
    }

    //endregion

    public SearchReturn search(String searchText, int limit, int offset, boolean preferPhraseSearching) {
        SearchReturn searchReturn = new SearchReturn();
        searchReturn.results = new ArrayList<>(0);
        searchReturn.suggestions = new ArrayList<>(0);

        return searchReturn;
    }

    public boolean removeFileFromIndex(String moduleId, String fileId) {
        return false;
    }
    public boolean indexFile(String moduleId, String fileid, String language, double boost, Map<String,String> searchableStrings, Map<String, String> fileMetadata) {
        return false;
    }
    public static String searchableStringFromString(String oldString) {
        return oldString;
    }
}
