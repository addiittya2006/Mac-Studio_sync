package com.zackliston.fulltextsearch.search_database;

import android.util.Log;

import com.zackliston.taskmanager.InternalWorkItem;
import com.zackliston.taskmanager.TaskFinishedInterface;

import org.apache.maven.artifact.ant.shaded.cli.Arg;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Zack Liston on 5/27/15.
 */
@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class SearchTaskWorkerTest {
    private SearchTaskWorker taskWorker;
    private TaskFinishedInterface mockTaskFinishedInterface;

    @Before
    public void setup() {
        SearchManager.getInstance().initialize(Robolectric.application);
        taskWorker = new SearchTaskWorker();

        mockTaskFinishedInterface = mock(TaskFinishedInterface.class);
        taskWorker.setTaskFinishedInterfaceForTest(mockTaskFinishedInterface);
    }

    @After
    public void tearDown() {
        mockTaskFinishedInterface = null;
        taskWorker = null;
        SearchManager.teardownForTest();
    }

    //region Test Setup
    @Test
    public void testSetupForWorkItem() throws Exception {
        String moduleId = "mod";
        String fileId = "filee";

        String url1 = "url1";
        String url2 = "url2";
        List<String> urlArray = new ArrayList<>(2);
        urlArray.add(url1);
        urlArray.add(url2);

        SearchManager.ActionType type = SearchManager.ActionType.INDEX_FILE;
        String databaseName = "dbName";
        SearchManager.getInstance().setupSearchDatabase(Robolectric.application, databaseName);
        SearchDatabase database = SearchManager.getInstance().searchDatabaseForName(databaseName);

        JSONObject jsonData = new JSONObject();
        JSONArray jsonURLs = new JSONArray(urlArray);
        jsonData.put(SearchTaskWorker.URL_ARRAY_KEY, jsonURLs);
        jsonData.put(SearchTaskWorker.ACTION_TYPE_KEY, type.getValue());
        jsonData.put(SearchTaskWorker.DATABASE_NAME_KEY, databaseName);
        jsonData.put(SearchTaskWorker.MODULE_ID_KEY, moduleId);
        jsonData.put(SearchTaskWorker.FILE_ID_KEY, fileId);

        InternalWorkItem workItem = new InternalWorkItem();
        InternalWorkItem mockWorkItem = spy(workItem);
        doReturn(jsonData).when(mockWorkItem).getJsonData();

        taskWorker.setupWithWorkItem(mockWorkItem);
        assertThat(taskWorker.type, is(type));
        assertThat(taskWorker.urlArray, is(urlArray));
        assertThat(taskWorker.searchDatabase, is(database));
        assertThat(taskWorker.moduleId, is(moduleId));
        assertThat(taskWorker.fileId, is(fileId));
    }
    //endregion

    //region Test Run
    @Test
    public void testRunRemoveSuccess() {
        String moduleId = "123mod";
        String fileId = "123File";
        SearchDatabase mockDb = mock(SearchDatabase.class);

        taskWorker.moduleId = moduleId;
        taskWorker.fileId = fileId;
        taskWorker.searchDatabase = mockDb;
        taskWorker.type = SearchManager.ActionType.REMOVE_FILE_FROM_INDEX;

        when(mockDb.removeFileFromIndex(anyString(), anyString())).thenReturn(true);

        taskWorker.run();

        verify(mockDb).removeFileFromIndex(eq(moduleId), eq(fileId));
        verify(mockTaskFinishedInterface).taskWorkerFinishedSuccessfully(eq(taskWorker), eq(true));
    }

    @Test
    public void testRunRemoveFailure() {
        String moduleId = "123mod";
        String fileId = "123File";
        SearchDatabase mockDb = mock(SearchDatabase.class);

        taskWorker.moduleId = moduleId;
        taskWorker.fileId = fileId;
        taskWorker.searchDatabase = mockDb;
        taskWorker.type = SearchManager.ActionType.REMOVE_FILE_FROM_INDEX;

        when(mockDb.removeFileFromIndex(anyString(), anyString())).thenReturn(false);

        taskWorker.run();

        verify(mockDb).removeFileFromIndex(eq(moduleId), eq(fileId));
        verify(mockTaskFinishedInterface).taskWorkerFinishedSuccessfully(eq(taskWorker), eq(false));
    }

    @Test
    public void testRunIndexSuccess() throws Exception {
        String url1 = "oneURL";
        String url2 = "twoURL";

        taskWorker.urlArray = new ArrayList<>(2);
        taskWorker.urlArray.add(url1);
        taskWorker.urlArray.add(url2);
        taskWorker.type = SearchManager.ActionType.INDEX_FILE;

        SearchTaskWorker mockWorker = spy(taskWorker);
        doNothing().when(mockWorker).taskFinishedWasSuccessful(anyBoolean());
        when(mockWorker.indexFileFromURL(anyString())).thenReturn(true);

        mockWorker.run();
        verify(mockWorker).indexFileFromURL(eq(url1));
        verify(mockWorker).indexFileFromURL(eq(url2));
        verify(mockWorker).taskFinishedWasSuccessful(eq(true));
    }

    @Test
    public void testRunIndexFailure() throws Exception {
        String url1 = "oneURL";
        String url2 = "twoURL";

        taskWorker.urlArray = new ArrayList<>(2);
        taskWorker.urlArray.add(url1);
        taskWorker.urlArray.add(url2);
        taskWorker.type = SearchManager.ActionType.INDEX_FILE;

        SearchTaskWorker mockWorker = spy(taskWorker);
        doNothing().when(mockWorker).taskFinishedWasSuccessful(anyBoolean());
        when(mockWorker.indexFileFromURL(eq(url1))).thenReturn(true);
        when(mockWorker.indexFileFromURL(eq(url2))).thenReturn(false);

        mockWorker.run();
        verify(mockWorker).indexFileFromURL(eq(url1));
        verify(mockWorker).indexFileFromURL(eq(url2));
        verify(mockWorker).taskFinishedWasSuccessful(eq(false));
    }
    //endregion

    //region Test Index
    @Test
    public void testIndexFileFromURLSuccess() throws Exception {
        String moduleId = "Mod234";
        String fileId = "file222";
        String language = "3n";
        double boost = 22.53445;
        Map<String, String> searchableStrings = new HashMap<>(1);
        searchableStrings.put("one", "two");
        Map<String, String> fileMetadata = new HashMap<>(1);
        fileMetadata.put("two", "three");

        String relativeURL = SearchManager.relativeURLForFileIndexInfo(moduleId, fileId);
        String absoluteURL = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(relativeURL);
        JSONObject jsonSearchableStrings = new JSONObject(searchableStrings);
        JSONObject jsonFileMetadata = new JSONObject(fileMetadata);

        JSONObject data = new JSONObject();
        data.put(SearchTaskWorker.MODULE_ID_KEY, moduleId);
        data.put(SearchTaskWorker.FILE_ID_KEY, fileId);
        data.put(SearchTaskWorker.LANGUAGE_KEY, language);
        data.put(SearchTaskWorker.BOOST_KEY, boost);
        data.put(SearchTaskWorker.SEARCHABLE_STRINGS_KEY, jsonSearchableStrings);
        data.put(SearchTaskWorker.FILE_METADATA_KEY, jsonFileMetadata);

        SearchManager.writeJSONToFile(absoluteURL, data);

        SearchDatabase mockDB = mock(SearchDatabase.class);
        when(mockDB.indexFile(anyString(), anyString(), anyString(), anyDouble(), anyMap(), anyMap())).thenReturn(true);
        taskWorker.searchDatabase = mockDB;
        taskWorker.succeededIndexFileInfoMaps = new ArrayList<>(1);

        boolean success = taskWorker.indexFileFromURL(relativeURL);
        assertThat(success, is(true));
        verify(mockDB).indexFile(eq(moduleId), eq(fileId), eq(language), eq(boost), eq(searchableStrings), eq(fileMetadata));

        assertThat(taskWorker.succeededIndexFileInfoMaps.size(), is(1));
        Map<String, String> map = taskWorker.succeededIndexFileInfoMaps.get(0);
        assertThat(map.get(SearchTaskWorker.MODULE_ID_KEY), is(moduleId));
        assertThat(map.get(SearchTaskWorker.FILE_ID_KEY), is(fileId));
        assertThat(map.get(SearchTaskWorker.URL_KEY), is(relativeURL));
    }

    @Test
    public void testIndexFileFromURLFailure() throws Exception {
        String moduleId = "Mod234";
        String fileId = "file222";
        String language = "3n";
        double boost = 22.53445;
        Map<String, String> searchableStrings = new HashMap<>(1);
        searchableStrings.put("one", "two");
        Map<String, String> fileMetadata = new HashMap<>(1);
        fileMetadata.put("two", "three");

        String relativeURL = SearchManager.relativeURLForFileIndexInfo(moduleId, fileId);
        String absoluteURL = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(relativeURL);
        JSONObject jsonSearchableStrings = new JSONObject(searchableStrings);
        JSONObject jsonFileMetadata = new JSONObject(fileMetadata);

        JSONObject data = new JSONObject();
        data.put(SearchTaskWorker.MODULE_ID_KEY, moduleId);
        data.put(SearchTaskWorker.FILE_ID_KEY, fileId);
        data.put(SearchTaskWorker.LANGUAGE_KEY, language);
        data.put(SearchTaskWorker.BOOST_KEY, boost);
        data.put(SearchTaskWorker.SEARCHABLE_STRINGS_KEY, jsonSearchableStrings);
        data.put(SearchTaskWorker.FILE_METADATA_KEY, jsonFileMetadata);

        SearchManager.writeJSONToFile(absoluteURL, data);

        SearchDatabase mockDB = mock(SearchDatabase.class);
        when(mockDB.indexFile(anyString(), anyString(), anyString(), anyDouble(), anyMap(), anyMap())).thenReturn(false);
        taskWorker.searchDatabase = mockDB;
        taskWorker.succeededIndexFileInfoMaps = new ArrayList<>(1);

        boolean success = taskWorker.indexFileFromURL(relativeURL);
        assertThat(success, is(false));
        verify(mockDB).indexFile(eq(moduleId), eq(fileId), eq(language), eq(boost), eq(searchableStrings), eq(fileMetadata));

        assertThat(taskWorker.succeededIndexFileInfoMaps.size(), is(0));
    }
    //endregion

    //region Test Finished
    @SuppressWarnings("unchecked")
    @Test
    public void testTaskFinishedWasSuccessfulForIndexType() throws Exception {
        String relUrl1 = "fileThing";
        String relUrl2 = "fileOther";
        String absUrl1 = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(relUrl1);
        String absUrl2 = SearchManager.absoluteURLForFileIndexInfoFromRelativeURL(relUrl2);
        String moduleId1 = "mod1";
        String moduleId2 = "mod2";
        String fileId1 = "file1";
        String fileId2 = "file2";

        Map<String, String> indexInfo1 = new HashMap<>(3);
        indexInfo1.put(SearchTaskWorker.URL_KEY, relUrl1);
        indexInfo1.put(SearchTaskWorker.MODULE_ID_KEY, moduleId1);
        indexInfo1.put(SearchTaskWorker.FILE_ID_KEY, fileId1);

        Map<String, String> indexInfo2 = new HashMap<>(3);
        indexInfo2.put(SearchTaskWorker.URL_KEY, relUrl2);
        indexInfo2.put(SearchTaskWorker.MODULE_ID_KEY, moduleId2);
        indexInfo2.put(SearchTaskWorker.FILE_ID_KEY, fileId2);

        JSONObject data = new JSONObject();
        List<String> urls = new ArrayList<>(3);
        urls.add(relUrl1);
        urls.add(relUrl2);
        urls.add("other");
        JSONArray remainingURLs = new JSONArray(urls);
        data.put(SearchTaskWorker.URL_ARRAY_KEY, remainingURLs);

        InternalWorkItem workItem = new InternalWorkItem();
        InternalWorkItem mockWorkItem = spy(workItem);
        when(mockWorkItem.getJsonData()).thenReturn(data);

        SearchManager.SearchWorkerProtocol mockProtocol = mock(SearchManager.SearchWorkerProtocol.class);


        taskWorker.succeededIndexFileInfoMaps = new ArrayList<>(2);
        taskWorker.succeededIndexFileInfoMaps.add(indexInfo1);
        taskWorker.succeededIndexFileInfoMaps.add(indexInfo2);
        taskWorker.type = SearchManager.ActionType.INDEX_FILE;
        taskWorker.setWorkItem(mockWorkItem);
        taskWorker.delegate = mockProtocol;

        SearchManager.writeJSONToFile(absUrl1, new JSONObject("{one:two}"));
        SearchManager.writeJSONToFile(absUrl2, new JSONObject("{three:four}"));

        taskWorker.taskFinishedWasSuccessful(true);

        JSONObject read1 = SearchManager.readJSONFromFile(absUrl1);
        JSONObject read2 = SearchManager.readJSONFromFile(absUrl2);
        assertThat(read1, nullValue());
        assertThat(read2, nullValue());

        JSONObject newData = taskWorker.workItem().getJsonData();
        JSONArray newURLArray = newData.getJSONArray(SearchTaskWorker.URL_ARRAY_KEY);
        assertThat(newURLArray.length(), is(1));
        assertThat(newURLArray.getString(0), is("other"));

        verify(mockTaskFinishedInterface).taskWorkerFinishedSuccessfully(eq(taskWorker), eq(true));
        ArgumentCaptor<List> moduleIDsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> fileIDsCaptor = ArgumentCaptor.forClass(List.class);
        verify(mockProtocol).searchWorkerIndexedFiles(moduleIDsCaptor.capture(), fileIDsCaptor.capture());

        List<String> capturedModuleIds = moduleIDsCaptor.getValue();
        List<String> capturedFileIds = fileIDsCaptor.getValue();

        assertThat(capturedModuleIds.size(), is(2));
        assertThat(capturedModuleIds.contains(moduleId1), is(true));
        assertThat(capturedModuleIds.contains(moduleId2), is(true));

        assertThat(capturedFileIds.size(), is(2));
        assertThat(capturedFileIds.contains(fileId1), is(true));
        assertThat(capturedFileIds.contains(fileId2), is(true));
    }

    @Test
    public void testTaskFinishedWasSuccessfulForRemoveType() throws Exception {
        SearchManager.SearchWorkerProtocol mockProtocol = mock(SearchManager.SearchWorkerProtocol.class);

        taskWorker.type = SearchManager.ActionType.REMOVE_FILE_FROM_INDEX;
        taskWorker.delegate = mockProtocol;

        taskWorker.taskFinishedWasSuccessful(true);

        verify(mockTaskFinishedInterface).taskWorkerFinishedSuccessfully(eq(taskWorker), eq(true));
        verify(mockProtocol, never()).searchWorkerIndexedFiles(anyList(), anyList());
    }
    //endregion
}