package com.zackliston.fulltextsearch.search_database;

import android.util.Log;

import com.zackliston.taskmanager.InternalWorkItem;
import com.zackliston.taskmanager.TaskFinishedInterface;
import com.zackliston.taskmanager.TaskWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Zack Liston on 6/2/15.
 */
public class SearchTaskWorker extends TaskWorker
{
    //region Constants
    public static final String ACTION_TYPE_KEY = "type";
    public static final String URL_ARRAY_KEY = "urlarray";
    static final String URL_KEY = "url";

    public static final String MODULE_ID_KEY = "moduleid";
    public static final String FILE_ID_KEY = "fileid";
    public static final String LANGUAGE_KEY = "language";
    public static final String BOOST_KEY = "boost";
    public static final String SEARCHABLE_STRINGS_KEY = "searchablestrings";
    public static final String FILE_METADATA_KEY = "filemetadata";
    public static final String DATABASE_NAME_KEY = "databasename";
    //endregion

    //region Properties
    SearchManager.SearchWorkerProtocol delegate;
    String moduleId;
    String fileId;
    SearchManager.ActionType type;
    List<String> urlArray;
    SearchDatabase searchDatabase;
    List<Map<String, String>> succeededIndexFileInfoMaps;
    //endregion

    //region Setup
    @Override
    public void setupWithWorkItem(InternalWorkItem workItem) {
        super.setupWithWorkItem(workItem);
        if (workItem.getJsonData() == null) {
            Log.e("SearchTaskWorker", "Failed setting up workItem - there is no data in the workItem");
            return;
        }
        try {
            JSONArray jsonUrlArray = workItem.getJsonData().getJSONArray(URL_ARRAY_KEY);
            int typeValue = workItem.getJsonData().getInt(ACTION_TYPE_KEY);
            String searchDBName = workItem.getJsonData().getString(DATABASE_NAME_KEY);

            if (workItem.getJsonData().has(MODULE_ID_KEY)) {
                moduleId = workItem.getJsonData().getString(MODULE_ID_KEY);
            }
            if (workItem.getJsonData().has(FILE_ID_KEY)) {
                fileId = workItem.getJsonData().getString(FILE_ID_KEY);
            }

            urlArray = new ArrayList<>(jsonUrlArray.length());
            for (int i=0; i<jsonUrlArray.length(); i++) {
                urlArray.add(jsonUrlArray.getString(i));
            }
            type = SearchManager.ActionType.actionTypeFromValue(typeValue);
            searchDatabase = SearchManager.getInstance().searchDatabaseForName(searchDBName);
            succeededIndexFileInfoMaps = new ArrayList<>(urlArray.size());
        } catch (JSONException exception) {
            Log.e("SearchTaskWorker", "Could not setupWithWorkItem because there was an error getting the data from the workItem " + exception);
        }
    }
    //endregion

    //region Run
    @Override
    public void run() {
        if (isCancelled()) {
            taskFinishedWasSuccessful(false);
            return;
        }
        boolean success = true;
        if (type == SearchManager.ActionType.REMOVE_FILE_FROM_INDEX) {
            success = searchDatabase.removeFileFromIndex(moduleId, fileId);
        } else if (type == SearchManager.ActionType.INDEX_FILE) {
            for (String url: urlArray) {
                if (isCancelled()) {
                    taskFinishedWasSuccessful(false);
                    return;
                }
                boolean indexSuccess = indexFileFromURL(url);
                if (!indexSuccess) {
                    success = false;
                }
            }
        }

        taskFinishedWasSuccessful(success);
    }
    //endregion

    //region Index
    boolean indexFileFromURL(String url) {
        String absoluteURL = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(url);
        JSONObject data = SearchManager.readJSONFromFile(absoluteURL);
        if (data == null) {
            Log.e("SearchTaskWorker", "Could not read JSON data on disk");
            return false;
        }
        boolean success = false;
        try {
            String moduleId = data.getString(MODULE_ID_KEY);
            String fileId = data.getString(FILE_ID_KEY);
            String language = data.getString(LANGUAGE_KEY);
            double boost = data.getDouble(BOOST_KEY);
            JSONObject jsonSearchableStrings = data.getJSONObject(SEARCHABLE_STRINGS_KEY);
            JSONObject jsonFileMetadata = data.getJSONObject(FILE_METADATA_KEY);

            Map<String, String> searchableStrings = new HashMap<>(jsonSearchableStrings.length());
            Iterator<String> searchableIterator = jsonSearchableStrings.keys();
            while (searchableIterator.hasNext()) {
                String key = searchableIterator.next();
                String text = jsonSearchableStrings.getString(key);
                searchableStrings.put(key, text);
            }
            Map<String, String> fileMetadata = new HashMap<>(jsonFileMetadata.length());
            Iterator<String> metaIterator = jsonFileMetadata.keys();
            while (metaIterator.hasNext()) {
                String key = metaIterator.next();
                String text = jsonFileMetadata.getString(key);
                fileMetadata.put(key, text);
            }

            success = searchDatabase.indexFile(moduleId, fileId, language, boost, searchableStrings, fileMetadata);

            if (success) {
                Map<String, String> fileIndexInfoMap = new HashMap<>(3);
                fileIndexInfoMap.put(MODULE_ID_KEY, moduleId);
                fileIndexInfoMap.put(FILE_ID_KEY, fileId);
                fileIndexInfoMap.put(URL_KEY, url);
                succeededIndexFileInfoMaps.add(fileIndexInfoMap);
            }
        } catch (JSONException exception) {
            Log.e("SearchTaskWorker", "There was an error getting the data in indexFileFromURL " + url + " : " + exception);
            return false;
        }

        return success;
    }
    //endregion

    //region Task Finished
    @Override
    protected void taskFinishedWasSuccessful(boolean wasSuccessful) {
        if (isCancelled()) {
            super.taskFinishedWasSuccessful(false);
            return;
        }

        if (type == SearchManager.ActionType.REMOVE_FILE_FROM_INDEX) {
            super.taskFinishedWasSuccessful(wasSuccessful);
            return;
        }

        List<String> remainingUrls = new ArrayList<>();
        try {
            JSONArray existingURLArray = workItem().getJsonData().getJSONArray(URL_ARRAY_KEY);
            for (int i=0; i<existingURLArray.length(); i++) {
                remainingUrls.add(existingURLArray.getString(i));
            }
        } catch (JSONException exception) {
            Log.e("SearchTaskWorker", "Error in taskFinishedWasSuccessful, could not get initial URL array from workItem");
        }

        List<String> completedFileIds = new ArrayList<>(succeededIndexFileInfoMaps.size());
        List<String> completedModuleIds = new ArrayList<>(succeededIndexFileInfoMaps.size());

        for (Map<String, String> indexFileInfo: succeededIndexFileInfoMaps) {
            String fileId = indexFileInfo.get(FILE_ID_KEY);
            String moduleId = indexFileInfo.get(MODULE_ID_KEY);
            completedFileIds.add(fileId);
            completedModuleIds.add(moduleId);

            String url = indexFileInfo.get(URL_KEY);
            String absoluteURL = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(url);
            File file = new File(absoluteURL);
            if (file.exists()) {
                boolean success = file.delete();
                if (!success) {
                    Log.e("SearchTaskWorker", "Could not delete indexFileInfo on disk in taskFinishedWasSuccessful URL : " + absoluteURL);
                }
            }
            remainingUrls.remove(url);
        }

        delegate.searchWorkerIndexedFiles(completedModuleIds, completedFileIds);
        try {
            JSONArray newURLArray = new JSONArray(remainingUrls);
            workItem().getJsonData().put(URL_ARRAY_KEY, newURLArray);
        } catch (JSONException exception) {
            Log.e("SearchTaskWorker", "Error updating workItem with new URL array after taskFinishedWasSuccessful " + exception);
        }

        super.taskFinishedWasSuccessful(wasSuccessful);
    }
    //endregion

    //region Test
    void setTaskFinishedInterfaceForTest(TaskFinishedInterface taskFinishedInterface) {
        setTaskFinishedDelegate(taskFinishedInterface);
    }

    @Override
    protected InternalWorkItem workItem() {
        return super.workItem();
    }
    @Override
    protected void setWorkItem(InternalWorkItem workItem) {
        super.setWorkItem(workItem);
    }
    //endregion
}
