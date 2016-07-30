package com.zackliston.fulltextsearch.search_database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zackliston.taskmanager.InternalWorkItem;
import com.zackliston.taskmanager.Manager;
import com.zackliston.taskmanager.Task;
import com.zackliston.taskmanager.TaskManager;
import com.zackliston.taskmanager.TaskWorker;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Zack Liston on 6/1/15.
 */
public class SearchManager extends Manager
{
    //region Callbacks
    public interface SearchCallback {
        void searchComplete(List<SearchResult> searchResults, List<String> searchSuggestions, String errorMessage);
    }
    //endregion

    //region Interfaces
    public interface IsSearchResultFavorited {
        boolean isSearchResultFavorited(SearchResult searchResult);
    }
    public interface BackupSearch {
        List<SearchResult> backupSearch(String searchText, int limit, int offset);
    }

    public interface SearchWorkerProtocol {
        void searchWorkerIndexedFiles(List<String> moduleIds, List<String> fileIds);
    }

    public interface RemoteSearch {
        boolean remoteSearch(String searchText, int limit, int offset, SearchCallback callback);
    }
    //endregion

    //region Constants
    public enum ActionType {
        INDEX_FILE(0),
        REMOVE_FILE_FROM_INDEX(1);

        private final int value;
        ActionType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static ActionType actionTypeFromValue(int value) {
            for (ActionType type: values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            return null;
        }
    }

    static final String SEARCH_INDEX_INFO_DIRECTORY_NAME = "ZLSearch_Index_Info";

    public static final String TASK_TYPE = "com.agilemd.tasktype.search";
    static final int MAJOR_PRIORITY = 1000;
    static final int MINOR_PRIORITY_INDEX = 1000;
    static final int MINOR_PRIORITY_REMOVE = 10000;

    static final String WEIGHT_0 = "weight0";
    static final String WEIGHT_1 = "weight1";
    static final String WEIGHT_2 = "weight2";
    static final String WEIGHT_3 = "weight3";
    static final String WEIGHT_4 = "weight4";
    static final String TITLE = "title";
    static final String SUBTITLE = "subtitle";
    static final String URI = "uri";
    static final String FILE_TYPE = "filetype";
    static final String IMAGE_URI = "imageuri";
    //endregion

    //region Properties
    private Context context;
    private ExecutorService backgroundExecutor = Executors.newCachedThreadPool();

    private IsSearchResultFavorited favoritedDelegate;
    private BackupSearch backupSearchDelegate;
    private RemoteSearch remoteSearchDelegate;
    SearchWorkerProtocol searchWorkerDelegate;

    Map<String, SearchDatabase> searchDatabaseMap = new HashMap<>();
    //endregion

    //region Setters
    public void setFavoritedDelegate(IsSearchResultFavorited delegate) {
        favoritedDelegate = delegate;
    }
    public void setBackupSearchDelegate(BackupSearch delegate) {
        backupSearchDelegate = delegate;
    }
    public void setRemoteSearchDelegate(RemoteSearch delegate) {
        remoteSearchDelegate = delegate;
    }
    public void setSearchWorkerDelegate(SearchWorkerProtocol delegate) {
        searchWorkerDelegate = delegate;
    }
    //endregion

    //region Getters
    public SearchDatabase searchDatabaseForName(String name) {
        return searchDatabaseMap.get(name);
    }

    TaskManager getTaskManager(Context context) {
        return TaskManager.getInstance(context);
    }
    //endregion

    //region Initialization
    private static SearchManager ourInstance;
    public static SearchManager getInstance() {
        if (ourInstance == null) {
            synchronized (SearchManager.class) {
                if (ourInstance == null) {
                    ourInstance = new SearchManager();
                }
            }
        }
        return ourInstance;
    }

    SearchManager() {}

    public void initialize(Context context) {
        this.context = context;
        setupFileDirectories(context);
    }
    //endregion

    //region Setup


    public void setupSearchDatabase(Context context, String name) {
        context = context.getApplicationContext();
        if (name == null || name.length() < 1) {
            Log.e("SearchManager", "Can not setup/create database with no name");
            return;
        }
        if (searchDatabaseForName(name) != null) {
            return;
        }
        SearchDatabase database = new SearchDatabase(context, name);
        searchDatabaseMap.put(name, database);
    }

    void setupFileDirectories(Context context) {
        File directory = new File(context.getFilesDir(), SEARCH_INDEX_INFO_DIRECTORY_NAME);
        if (!directory.exists()) {
            boolean createSuccess = directory.mkdirs();
            if (!createSuccess) {
                Log.e("FileManager", "Could not create directory for " + SEARCH_INDEX_INFO_DIRECTORY_NAME) ;
            }
        }
    }
    //endregion

    //region TaskManager Manager Methods
    @Override
    protected TaskWorker taskWorkerForWorkItem(InternalWorkItem internalWorkItem) {
        TaskWorker worker = null;
        if (internalWorkItem.getTaskType().equals(TASK_TYPE)) {
            SearchTaskWorker searchWorker = new SearchTaskWorker();
            searchWorker.delegate = searchWorkerDelegate;
            worker = searchWorker;
        } else {
            Log.e("SearchManager", "Error in taskWorkerForWorkItem unrecognized task type " + internalWorkItem.getTaskType());
        }
        if (worker != null) {
            worker.setupWithWorkItem(internalWorkItem);
        }
        return worker;
    }
    //endregion

    //region Public Methods
    //region Queue Search Tasks
    @Nullable
    public static String saveIndexFileInfoToFile(String moduleId, String fileId, String language, double boost, Map<String, String> searchableStrings, Map<String, String> fileMetadata) throws InvalidParameterException {
        boolean hasSearchableText = false;
        for (String key: searchableStrings.keySet()) {
            if (searchableStrings.get(key).length() > 0) {
                hasSearchableText = true;
                break;
            }
        }
        if (moduleId == null || moduleId.length() < 1 || fileId == null || fileId.length() < 1 || language == null || language.length() < 1 || !hasSearchableText) {
            throw new InvalidParameterException("Missing required fields");
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(SearchTaskWorker.MODULE_ID_KEY, moduleId);
            jsonObject.put(SearchTaskWorker.FILE_ID_KEY, fileId);
            jsonObject.put(SearchTaskWorker.LANGUAGE_KEY, language);
            jsonObject.put(SearchTaskWorker.BOOST_KEY, boost);

            JSONObject searchableStringsJson = new JSONObject();
            for (String key: searchableStrings.keySet()) {
                searchableStringsJson.put(key, searchableStrings.get(key));
            }
            JSONObject fileMetadataJson = new JSONObject();
            for (String key: fileMetadata.keySet()) {
                fileMetadataJson.put(key, fileMetadata.get(key));
            }

            jsonObject.put(SearchTaskWorker.SEARCHABLE_STRINGS_KEY, searchableStringsJson);
            jsonObject.put(SearchTaskWorker.FILE_METADATA_KEY, fileMetadataJson);
        } catch (JSONException exception) {
            throw new InvalidParameterException("Could not parse parameters into json: " + exception.getMessage());
        }

        String relativeFileLocation = relativeURLForFileIndexInfo(moduleId, fileId);
        String absoluteFileLocation = absoluteURLForFileIndexInfoFromRelativeURL(relativeFileLocation);

        boolean writeSuccess = writeJSONToFile(absoluteFileLocation, jsonObject);
        if (writeSuccess) {
            return relativeFileLocation;
        }
        return null;
    }

    public boolean queueIndexFileCollection(List<String> urlArray, String databaseName) {
        if (urlArray == null || urlArray.size() < 1) {
            Log.e("SearchManager", "Error in queueIndexFileCollection. URL array contained no URLs");
            return false;
        }
        if (databaseName == null || databaseName.length() < 1) {
            Log.e("SearchManager", "Error in queueIndexFileCollection. You must provide a search database name");
            return false;
        }
        JSONArray jsonUrlArray = new JSONArray(urlArray);
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put(SearchTaskWorker.URL_ARRAY_KEY, jsonUrlArray);
            jsonData.put(SearchTaskWorker.ACTION_TYPE_KEY, ActionType.INDEX_FILE.getValue());
            jsonData.put(SearchTaskWorker.DATABASE_NAME_KEY, databaseName);
        } catch (JSONException exception) {
            Log.e("SearchManager", "Error in queueIndexFileCollection could not create json data " + exception);
            return false;
        }


        Task task = new Task(TASK_TYPE, jsonData);
        task.setMajorPriority(MAJOR_PRIORITY);
        task.setMinorPriority(MINOR_PRIORITY_INDEX);
        task.setRequiresInternet(false);
        task.setShouldHoldAfterMaxRetries(true);

        return getTaskManager(context).queueTask(task);
    }

    public boolean queueIndexFile(String moduleId, String fileId, String language, double boost, Map<String, String> searchableStrings, Map<String, String> fileMetadata, String searchDatabaseName) {
        String fileLocation;
        try {
            fileLocation = saveIndexFileInfoToFile(moduleId, fileId, language, boost, searchableStrings, fileMetadata);
        } catch (InvalidParameterException exception) {
            Log.e("SearchManager", "Error in queueIndexFile error saving search info to file " + exception);
            return false;
        }

        List<String> urlArray = new ArrayList<>(1);
        urlArray.add(fileLocation);
        return queueIndexFileCollection(urlArray, searchDatabaseName);
    }

    public boolean queueRemoveFile(String moduleId, String fileId, String searchDatabaseName) {
        if (moduleId == null || moduleId.length() < 1 || fileId == null || fileId.length() < 1 || searchDatabaseName == null || searchDatabaseName.length() < 1) {
            Log.e("SearchManager", "Error in queueRemoveFile - one or more paramter is missing. Cannot queue");
            return false;
        }

        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put(SearchTaskWorker.MODULE_ID_KEY, moduleId);
            jsonData.put(SearchTaskWorker.FILE_ID_KEY, fileId);
            jsonData.put(SearchTaskWorker.ACTION_TYPE_KEY, ActionType.REMOVE_FILE_FROM_INDEX.getValue());
            jsonData.put(SearchTaskWorker.DATABASE_NAME_KEY, searchDatabaseName);
        } catch (JSONException exception) {
            Log.e("SearchManager", "Could not parse input into json for Task in queueRemoveFile " + exception);
            return false;
        }
        Task task = new Task(TASK_TYPE, jsonData);
        task.setMajorPriority(MAJOR_PRIORITY);
        task.setMinorPriority(MINOR_PRIORITY_REMOVE);
        task.setRequiresInternet(false);
        task.setShouldHoldAfterMaxRetries(true);

        return getTaskManager(context).queueTask(task);
    }
    //endregion

    //region Search
    public boolean localSearch(final String searchText, final int limit, final int offset, final String searchDatabaseName, final SearchCallback searchCallback) {
        boolean success = true;
        if (limit < 1) {
            return success;
        }
        if (searchCallback == null) {
            Log.e("SearchManager", "Cannot perform search in localSearch because no callback was specified.");
            return false;
        }

        backgroundExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<SearchResult> results;

                SearchDatabase searchDatabase = searchDatabaseForName(searchDatabaseName);
                final SearchDatabase.SearchReturn searchReturn = searchDatabase.search(searchText, limit, offset, true);
                results = searchReturn.getResults();

                if (results.size() > 0) {
                    for (SearchResult result : results) {
                        result.isSearchResultFavoritedDelegate = SearchManager.this.favoritedDelegate;
                    }
                } else {
                    if (backupSearchDelegate != null) {
                        results = backupSearchDelegate.backupSearch(searchText, limit, offset);
                    }
                }

                final List<SearchResult> finalResults = results;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        searchCallback.searchComplete(finalResults, searchReturn.getSuggestions(), null);
                    }
                });
            }
        });
        return true;
    }

    public boolean fullSearch(String searchText, int limit, int offset, String searchDatabaseName, SearchCallback localSearchCallback, SearchCallback remoteSearchCallback) {
        boolean localSuccess = localSearch(searchText, limit, offset, searchDatabaseName, localSearchCallback);
        boolean remoteSuccess = true;
        if (remoteSearchDelegate != null) {
            remoteSuccess = remoteSearchDelegate.remoteSearch(searchText, limit, offset, remoteSearchCallback);
        }
        return (localSuccess && remoteSuccess);
    }
    //endregion
    //endregion

    //region File Operations
    static boolean writeJSONToFile(String fileLocation, JSONObject jsonObject) {
        boolean success = false;
        try {
            FileUtils.writeStringToFile(new File(fileLocation), jsonObject.toString());
            success = true;
        } catch (IOException exception) {
            exception.printStackTrace();
            Log.e("SearchManager", "Error writing map to file location " + fileLocation + " Exception: " + exception);
        }
        return success;
    }

    static JSONObject readJSONFromFile(String fileLocation) {
        JSONObject jsonObject = null;
        try {
            String jsonString = FileUtils.readFileToString(new File(fileLocation));
            jsonObject = new JSONObject(jsonString);

        } catch (IOException | JSONException exception) {
            Log.e("SearchManager", "Error reading map from file " + exception);
        }
        return jsonObject;
    }

    void resetFileIndexInfoCache() {
        if (context == null) {
            Log.e("SearchManager", "Error resetting fileIndexInfoCache because the SearchManager has not been initialized");
        }
        File directory = new File(context.getFilesDir(), SEARCH_INDEX_INFO_DIRECTORY_NAME);
        if (directory.exists()) {
            try {
                FileUtils.deleteDirectory(directory);
            } catch (IOException exception) {
                Log.e("SearchManager", "Could not delete old cache directory " + exception);
            }
        }
        setupFileDirectories(context);
    }
    //endregion

    //region Helpers
    public static String relativeURLForFileIndexInfo(String moduleId, String fileId) {
        return SEARCH_INDEX_INFO_DIRECTORY_NAME + "/" + moduleId + "." + fileId + ".json";
    }

    public static String absoluteURLForFileIndexInfoFromRelativeURL(String relativeUrl) {
        if (SearchManager.getInstance().context == null) {
            Log.e("SearchManager", "Cannot get absoluteURLForFileIndexInfo because the SearchManager has not been initialized");
            return null;
        }
        return SearchManager.getInstance().context.getFilesDir() + "/" + relativeUrl;
    }
    //endregion

    //region Test
    static void teardownForTest() {
        ourInstance = null;
    }
    //endregion
}
