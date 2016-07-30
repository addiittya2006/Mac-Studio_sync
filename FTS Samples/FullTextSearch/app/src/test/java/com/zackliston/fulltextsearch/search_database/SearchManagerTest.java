package com.zackliston.fulltextsearch.search_database;

import android.content.Context;
import android.util.Log;

import com.zackliston.taskmanager.InternalWorkItem;
import com.zackliston.taskmanager.Task;
import com.zackliston.taskmanager.TaskManager;
import com.zackliston.taskmanager.TaskWorker;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Zack Liston on 5/27/15.
 */
@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class SearchManagerTest {
    @Before
    public void setup() {
        SearchManager.getInstance().initialize(Robolectric.application);
        SearchManager.getInstance().resetFileIndexInfoCache();
    }

    @After
    public void tearDown() {
        SearchManager.getInstance().resetFileIndexInfoCache();
        SearchManager.teardownForTest();
    }

    //region Test Initialize
    @Test
    public void testInitialize() {
        Context context = Robolectric.application;
        SearchManager manager = new SearchManager();

        SearchManager mockManager = spy(manager);

        mockManager.initialize(context);

        manager.initialize(context);
        verify(mockManager).setupFileDirectories(eq(context));
    }
    //endregion

    //region Test Setup
    @Test
    public void testSetupSearchDatabase() throws Exception {
        String name = "dbName";

        SearchDatabase beforeDatabase = SearchManager.getInstance().searchDatabaseMap.get(name);
        assertThat(beforeDatabase, nullValue());

        SearchManager.getInstance().setupSearchDatabase(Robolectric.application, name);

        SearchDatabase afterDatabase = SearchManager.getInstance().searchDatabaseMap.get(name);
        assertThat(afterDatabase, notNullValue());
        assertThat(afterDatabase.getDatabaseName(), is(name));
    }

    @Test
    public void testSetupFileSystem() {
        Context context = Robolectric.application;

        SearchManager.getInstance().setupFileDirectories(context);

        File directoryAfter = new File(context.getFilesDir(), SearchManager.SEARCH_INDEX_INFO_DIRECTORY_NAME);
        assertThat(directoryAfter.exists(), is(true));
    }
    //endregion

    //region Test Getters
    @Test
    public void testSearchDatabaseForName() throws Exception {
        String name = "other";

        SearchDatabase beforeDatabase = SearchManager.getInstance().searchDatabaseForName(name);
        assertThat(beforeDatabase, nullValue());

        SearchDatabase newDatabase = new SearchDatabase(Robolectric.application, name);
        SearchManager.getInstance().searchDatabaseMap.put(name, newDatabase);

        SearchDatabase afterDatabase = SearchManager.getInstance().searchDatabaseForName(name);
        assertThat(afterDatabase, notNullValue());
        assertThat(afterDatabase, is(newDatabase));
    }
    //endregion

    //region Test SaveIndexFileInfo
    @Test
    public void testSaveIndexFileInfo() throws Exception {
        String moduleId = "mod";
        String fileId = "file22";
        String language = "en";
        double boost = 8323.3;
        String weight0 = "sometext";
        String weight3 = "othertext";
        String fileType = "doc";
        Map<String, String> searchableStrings = new HashMap<>(2);
        searchableStrings.put(SearchManager.WEIGHT_0, weight0);
        searchableStrings.put(SearchManager.WEIGHT_3, weight3);

        Map<String, String> metadata = new HashMap<>(1);
        metadata.put(SearchManager.FILE_TYPE, fileType);

        String returnedFileLocation = null;
        try {
            returnedFileLocation = SearchManager.saveIndexFileInfoToFile(moduleId, fileId, language, boost, searchableStrings, metadata);
        } catch (InvalidParameterException exception) {
            assertThat("This method is given appropriate input. It shouldn't throw an exception", false);
        }

        String relativeFileLocation = SearchManager.relativeURLForFileIndexInfo(moduleId, fileId);
        assertThat(returnedFileLocation, is(relativeFileLocation));

        String absoluteFileLocation = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(relativeFileLocation);

        JSONObject jsonObject = SearchManager.readJSONFromFile(absoluteFileLocation);
        String readModuleId = jsonObject.getString(SearchTaskWorker.MODULE_ID_KEY);
        String readFileId = jsonObject.getString(SearchTaskWorker.FILE_ID_KEY);
        String readLanguage = jsonObject.getString(SearchTaskWorker.LANGUAGE_KEY);
        double readBoost = jsonObject.getDouble(SearchTaskWorker.BOOST_KEY);

        JSONObject readSearchableStrings = jsonObject.getJSONObject(SearchTaskWorker.SEARCHABLE_STRINGS_KEY);
        JSONObject readMetadata = jsonObject.getJSONObject(SearchTaskWorker.FILE_METADATA_KEY);

        assertThat(readModuleId, is(moduleId));
        assertThat(readFileId, is(fileId));
        assertThat(readLanguage, is(language));
        assertThat(readBoost, is(boost));

        assertThat(readSearchableStrings.length(), is(searchableStrings.size()));
        String readWeight0 = readSearchableStrings.getString(SearchManager.WEIGHT_0);
        String readWeight3 = readSearchableStrings.getString(SearchManager.WEIGHT_3);
        assertThat(readWeight0, is(weight0));
        assertThat(readWeight3, is(weight3));

        assertThat(readMetadata.length(), is(metadata.size()));
        String readFileType = readMetadata.getString(SearchManager.FILE_TYPE);
        assertThat(readFileType, is(fileType));
    }

    @Test
    public void testSaveIndexFileInfoNoModuleId() throws Exception {
        String moduleId = null;
        String fileId = "file22";
        String language = "en";
        double boost = 8323.3;
        String weight0 = "sometext";
        String weight3 = "othertext";
        String fileType = "doc";
        Map<String, String> searchableStrings = new HashMap<>(2);
        searchableStrings.put(SearchManager.WEIGHT_0, weight0);
        searchableStrings.put(SearchManager.WEIGHT_3, weight3);

        Map<String, String> metadata = new HashMap<>(1);
        metadata.put(SearchManager.FILE_TYPE, fileType);

        boolean didThrowException = false;
        String returnedFileLocation = null;
        try {
            returnedFileLocation = SearchManager.saveIndexFileInfoToFile(moduleId, fileId, language, boost, searchableStrings, metadata);
        } catch (InvalidParameterException exception) {
            didThrowException = true;
        }
        assertThat(didThrowException, is(true));
        assertThat(returnedFileLocation, nullValue());
    }

    @Test
    public void testSaveIndexFileInfoNoFileId() throws Exception {
        String moduleId = "mod";
        String fileId = null;
        String language = "en";
        double boost = 8323.3;
        String weight0 = "sometext";
        String weight3 = "othertext";
        String fileType = "doc";
        Map<String, String> searchableStrings = new HashMap<>(2);
        searchableStrings.put(SearchManager.WEIGHT_0, weight0);
        searchableStrings.put(SearchManager.WEIGHT_3, weight3);

        Map<String, String> metadata = new HashMap<>(1);
        metadata.put(SearchManager.FILE_TYPE, fileType);

        boolean didThrowException = false;
        String returnedFileLocation = null;
        try {
            returnedFileLocation = SearchManager.saveIndexFileInfoToFile(moduleId, fileId, language, boost, searchableStrings, metadata);
        } catch (InvalidParameterException exception) {
            didThrowException = true;
        }
        assertThat(didThrowException, is(true));
        assertThat(returnedFileLocation, nullValue());
    }

    @Test
    public void testSaveIndexFileInfoNoLanguage() throws Exception {
        String moduleId = "mod";
        String fileId = "file22";
        String language = null;
        double boost = 8323.3;
        String weight0 = "sometext";
        String weight3 = "othertext";
        String fileType = "doc";
        Map<String, String> searchableStrings = new HashMap<>(2);
        searchableStrings.put(SearchManager.WEIGHT_0, weight0);
        searchableStrings.put(SearchManager.WEIGHT_3, weight3);

        Map<String, String> metadata = new HashMap<>(1);
        metadata.put(SearchManager.FILE_TYPE, fileType);

        boolean didThrowException = false;
        String returnedFileLocation = null;
        try {
            returnedFileLocation = SearchManager.saveIndexFileInfoToFile(moduleId, fileId, language, boost, searchableStrings, metadata);
        } catch (InvalidParameterException exception) {
            didThrowException = true;
        }
        assertThat(didThrowException, is(true));
        assertThat(returnedFileLocation, nullValue());
    }

    @Test
    public void testSaveIndexFileInfoNoSearchableStrings() throws Exception {
        String moduleId = "mod";
        String fileId = "file22";
        String language = "en";
        double boost = 8323.3;
        String fileType = "doc";
        Map<String, String> searchableStrings = new HashMap<>(2);

        Map<String, String> metadata = new HashMap<>(1);
        metadata.put(SearchManager.FILE_TYPE, fileType);

        boolean didThrowException = false;
        String returnedFileLocation = null;
        try {
            returnedFileLocation = SearchManager.saveIndexFileInfoToFile(moduleId, fileId, language, boost, searchableStrings, metadata);
        } catch (InvalidParameterException exception) {
            didThrowException = true;
        }
        assertThat(didThrowException, is(true));
        assertThat(returnedFileLocation, nullValue());
    }
    //endregion

    //region Test Queue Index File
    @Test
    public void testQueueIndexFileCollection() throws Exception {
        String url1 = "file://one";
        String url2 = "file://two";
        List<String> urlArray = new ArrayList<>(2);
        urlArray.add(url1);
        urlArray.add(url2);

        String databaseName = "dbnameOne";

        SearchManager manager = new SearchManager();
        manager.initialize(Robolectric.application);

        SearchManager mockManager = spy(manager);

        TaskManager mockTaskManager = mock(TaskManager.class);
        doReturn(true).when(mockTaskManager).queueTask(Matchers.any(Task.class));

        doReturn(mockTaskManager).when(mockManager).getTaskManager(Matchers.any(Context.class));

        boolean success = mockManager.queueIndexFileCollection(urlArray, databaseName);
        assertThat(success, is(true));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(mockTaskManager).queueTask(captor.capture());

        Task task = captor.getValue();
        assertThat(task.getTaskType(), is(SearchManager.TASK_TYPE));
        assertThat(task.getMajorPriority(), is(SearchManager.MAJOR_PRIORITY));
        assertThat(task.getMinorPriority(), is(SearchManager.MINOR_PRIORITY_INDEX));
        assertThat(task.isRequiresInternet(), is(false));
        assertThat(task.isShouldHoldAfterMaxRetries(), is(true));

        JSONObject jsonData = task.getJsonData();
        JSONArray jsonUrlArray = jsonData.getJSONArray(SearchTaskWorker.URL_ARRAY_KEY);
        int typeValue = jsonData.getInt(SearchTaskWorker.ACTION_TYPE_KEY);
        String jsonDatabaseName = jsonData.getString(SearchTaskWorker.DATABASE_NAME_KEY);

        assertThat(jsonUrlArray.length(), is(urlArray.size()));
        assertThat(jsonUrlArray.getString(0), is(url1));
        assertThat(jsonUrlArray.getString(1), is(url2));

        assertThat(typeValue, is(SearchManager.ActionType.INDEX_FILE.getValue()));
        assertThat(jsonDatabaseName, is(databaseName));
    }

    @Test
    public void testQueueIndexFileCollectionNoURLS() throws Exception {
        List<String> urlArray = new ArrayList<>(2);

        String databaseName = "dbnameOne";

        SearchManager manager = new SearchManager();
        manager.initialize(Robolectric.application);

        SearchManager mockManager = spy(manager);

        TaskManager mockTaskManager = mock(TaskManager.class);
        doReturn(true).when(mockTaskManager).queueTask(Matchers.any(Task.class));

        doReturn(mockTaskManager).when(mockManager).getTaskManager(Matchers.any(Context.class));

        boolean success = mockManager.queueIndexFileCollection(urlArray, databaseName);
        assertThat(success, is(false));

        verify(mockTaskManager, never()).queueTask(Matchers.any(Task.class));
    }

    @Test
    public void testQueueIndexFileCollectionNoDatabaseName() throws Exception {
        String url1 = "file://one";
        String url2 = "file://two";
        List<String> urlArray = new ArrayList<>(2);
        urlArray.add(url1);
        urlArray.add(url2);

        String databaseName = null;

        SearchManager manager = new SearchManager();
        manager.initialize(Robolectric.application);

        SearchManager mockManager = spy(manager);

        TaskManager mockTaskManager = mock(TaskManager.class);
        doReturn(true).when(mockTaskManager).queueTask(Matchers.any(Task.class));

        doReturn(mockTaskManager).when(mockManager).getTaskManager(Matchers.any(Context.class));

        boolean success = mockManager.queueIndexFileCollection(urlArray, databaseName);
        assertThat(success, is(false));

        verify(mockTaskManager, never()).queueTask(Matchers.any(Task.class));

    }
    //endregion

    //region Test Queue Remove File
    @Test
    public void testQueueRemoveFile() throws Exception {
        String moduleId = "moaedasdf";
        String fileId = "file11";
        String searchDatabaseName = "searchDB12";

        SearchManager manager = new SearchManager();
        manager.initialize(Robolectric.application);

        SearchManager mockManager = spy(manager);

        TaskManager mockTaskManager = mock(TaskManager.class);
        doReturn(true).when(mockTaskManager).queueTask(Matchers.any(Task.class));

        doReturn(mockTaskManager).when(mockManager).getTaskManager(Matchers.any(Context.class));

        boolean success = mockManager.queueRemoveFile(moduleId, fileId, searchDatabaseName);
        assertThat(success, is(true));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(mockTaskManager).queueTask(captor.capture());

        Task task = captor.getValue();
        assertThat(task.getTaskType(), is(SearchManager.TASK_TYPE));
        assertThat(task.getMajorPriority(), is(SearchManager.MAJOR_PRIORITY));
        assertThat(task.getMinorPriority(), is(SearchManager.MINOR_PRIORITY_REMOVE));
        assertThat(task.isRequiresInternet(), is(false));
        assertThat(task.isShouldHoldAfterMaxRetries(), is(true));

        JSONObject jsonData = task.getJsonData();
        String jsonModuleId = jsonData.getString(SearchTaskWorker.MODULE_ID_KEY);
        String jsonFileId = jsonData.getString(SearchTaskWorker.FILE_ID_KEY);
        int type = jsonData.getInt(SearchTaskWorker.ACTION_TYPE_KEY);
        String jsonDatabaseName = jsonData.getString(SearchTaskWorker.DATABASE_NAME_KEY);

        assertThat(jsonModuleId, is(moduleId));
        assertThat(jsonFileId, is(fileId));
        assertThat(type, is(SearchManager.ActionType.REMOVE_FILE_FROM_INDEX.getValue()));
        assertThat(jsonDatabaseName, is(searchDatabaseName));
    }

    @Test
    public void testQueueRemoveFileNoModuleId() throws Exception {
        String moduleId = null;
        String fileId = "file11";
        String searchDatabaseName = "searchDB12";

        SearchManager manager = new SearchManager();
        manager.initialize(Robolectric.application);

        SearchManager mockManager = spy(manager);

        TaskManager mockTaskManager = mock(TaskManager.class);
        doReturn(true).when(mockTaskManager).queueTask(Matchers.any(Task.class));

        doReturn(mockTaskManager).when(mockManager).getTaskManager(Matchers.any(Context.class));

        boolean success = mockManager.queueRemoveFile(moduleId, fileId, searchDatabaseName);
        assertThat(success, is(false));
    }


    @Test
    public void testQueueRemoveFileNoFileId() throws Exception {
        String moduleId = "modddsd";
        String fileId = null;
        String searchDatabaseName = "searchDB12";

        SearchManager manager = new SearchManager();
        manager.initialize(Robolectric.application);

        SearchManager mockManager = spy(manager);

        TaskManager mockTaskManager = mock(TaskManager.class);
        doReturn(true).when(mockTaskManager).queueTask(Matchers.any(Task.class));

        doReturn(mockTaskManager).when(mockManager).getTaskManager(Matchers.any(Context.class));

        boolean success = mockManager.queueRemoveFile(moduleId, fileId, searchDatabaseName);
        assertThat(success, is(false));
    }

    @Test
    public void testQueueRemoveFileNoDatabaseName() throws Exception {
        String moduleId = "module";
        String fileId = "file11";
        String searchDatabaseName = null;

        SearchManager manager = new SearchManager();
        manager.initialize(Robolectric.application);

        SearchManager mockManager = spy(manager);

        TaskManager mockTaskManager = mock(TaskManager.class);
        doReturn(true).when(mockTaskManager).queueTask(Matchers.any(Task.class));

        doReturn(mockTaskManager).when(mockManager).getTaskManager(Matchers.any(Context.class));

        boolean success = mockManager.queueRemoveFile(moduleId, fileId, searchDatabaseName);
        assertThat(success, is(false));
    }
    //endregion

    //region Test File Operations
    @Test
    public void testSaveMapToFile() throws Exception {
        String relative = SearchManager.relativeURLForFileIndexInfo("module", "file");
        String absoluteFileLocation = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(relative);

        JSONObject jsonToSave = new JSONObject("{one:two}");

        boolean success = SearchManager.writeJSONToFile(absoluteFileLocation, jsonToSave);
        assertThat(success, is(true));

        String jsonString = FileUtils.readFileToString(new File(absoluteFileLocation));

        JSONObject readJson = new JSONObject(jsonString);
        assertThat(readJson.toString().equals(jsonToSave.toString()), is(true));
    }

    @Test
    public void testReadMapFromFile() throws Exception {
        String relative = SearchManager.relativeURLForFileIndexInfo("module123", "file123");
        String absoluteFileLocation = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(relative);

        JSONObject jsonToSave = new JSONObject();
        jsonToSave.put("one", "two");

        FileUtils.writeStringToFile(new File(absoluteFileLocation), jsonToSave.toString());

        JSONObject readJson = SearchManager.readJSONFromFile(absoluteFileLocation);
        assertThat(readJson, notNullValue());
        assertThat(readJson.toString().equals(jsonToSave.toString()), is(true));
    }

    @Test
    public void testResetFileIndexInfoDirectory() throws Exception {
        String relative = SearchManager.relativeURLForFileIndexInfo("module123", "file123");
        String absoluteFileLocation = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(relative);

        JSONObject jsonToSave = new JSONObject("{one:two}");

        SearchManager.writeJSONToFile(absoluteFileLocation, jsonToSave);

        JSONObject beforeJSON = SearchManager.readJSONFromFile(absoluteFileLocation);
        assertThat(beforeJSON.toString().equals(jsonToSave.toString()), is(true));

        SearchManager.getInstance().resetFileIndexInfoCache();

        JSONObject afterJson = SearchManager.readJSONFromFile(absoluteFileLocation);
        assertThat(afterJson, nullValue());

        File directory = new File(Robolectric.application.getFilesDir(), SearchManager.SEARCH_INDEX_INFO_DIRECTORY_NAME);
        assertThat(directory.exists(), is(true));
    }
    //endregion

    //region Test TaskWorker
    @Test
    public void testTaskWorkerForWorkItem() throws Exception {
        SearchManager.getInstance().setSearchWorkerDelegate(new SearchManager.SearchWorkerProtocol() {
            @Override
            public void searchWorkerIndexedFiles(List<String> moduleIds, List<String> fileIds) {

            }
        });

        InternalWorkItem workItem = new InternalWorkItem();
        InternalWorkItem mockWorkItem = spy(workItem);
        when(mockWorkItem.getTaskType()).thenReturn(SearchManager.TASK_TYPE);

        TaskWorker worker = SearchManager.getInstance().taskWorkerForWorkItem(mockWorkItem);
        assertThat(worker, notNullValue());
        assertThat((worker instanceof SearchTaskWorker), is(true));

        SearchTaskWorker searchTaskWorker = (SearchTaskWorker)worker;
        assertThat(searchTaskWorker.delegate, is(SearchManager.getInstance().searchWorkerDelegate));
    }
    //endregion
}