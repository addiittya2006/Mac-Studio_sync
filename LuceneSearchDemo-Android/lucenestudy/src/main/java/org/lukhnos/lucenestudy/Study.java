package org.lukhnos.lucenestudy;

/**
 * Copyright (c) 2015 Lukhnos Liu
 *
 * Licensed under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Study {
  public static void main(String args[]) throws Exception {
    if (args.length < 3) {
      showHelpAndExit();
      return;
    }

    if (args[0].equalsIgnoreCase("index")) {
      index(args[1], args[2]);
    } else if (args[0].equalsIgnoreCase("search")) {
      search(args[1], args[2]);
    } else if (args[0].equalsIgnoreCase("suggest")) {
      suggest(args[1], args[2]);
    }

  }

  static void showHelpAndExit() {
    System.err.println("Usage: Study [index|search|suggest] arguments...");
    System.err.println("    index <source JSON> <index path>");
    System.err.println("    search <index path> <query>");
    System.err.println("    suggest <index path> <keyword(s)>");
    System.exit(1);
  }

  static void index(String sourcePath, String indexPath) {
    File dataFile = new File(sourcePath);
    if (!dataFile.exists()) {
      System.err.println("JSON source not found: " + sourcePath);
      System.exit(1);
    }

    if (dataFile.length() > Integer.MAX_VALUE) {
      System.exit(1);
    }

    try (FileInputStream stream = new FileInputStream(sourcePath)) {
      importData(stream, indexPath, true);
    } catch (Exception e) {
      // Should not happen
      e.printStackTrace();
      System.exit(1);
    }
  }

  static public int importData(InputStream stream, String indexPath, boolean withSuggestion) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final int bufSize = 4096;
    byte[] buf = new byte[bufSize];
    int read;
    while ((read = stream.read(buf)) > 0) {
      baos.write(buf, 0, read);
    }
    String dataStr = new String(baos.toByteArray(), "UTF-8");

    List<Document> docs = new ArrayList<>();

    JSONArray jsonArray = new JSONArray(dataStr);

    for (int i = 0, len = jsonArray.length(); i < len; i++) {
      JSONObject entry = jsonArray.getJSONObject(i);
      String title = entry.getString("title");
      int year = entry.getInt("year");
      int rating = entry.getInt("rating");
      boolean positive = entry.getBoolean("positive");
      String review = entry.getString("review");
      String source = entry.getString("source");

      Document doc = new Document(title, year, rating, positive, review, source);
      docs.add(doc);
    }

    Indexer indexer = new Indexer(indexPath, false);
    indexer.addDocuments(docs);
    indexer.close();

    if (withSuggestion) {
      Suggester.rebuild(indexPath);
    }

    return docs.size();
  }

  static void search(String indexPath, String query) throws Exception {
    Searcher searcher = new Searcher(indexPath);
    SearchResult result = searcher.search(query, null, 10);

    for (Document doc : result.documents) {
      System.out.println("title   : " + result.getHighlightedTitle(doc));
      System.out.println("year    : " + doc.year);
      System.out.println("rating  : " + doc.rating);
      System.out.println("positive: " + doc.positive);
      System.out.println("review  : " + result.getHighlightedReview(doc));
      System.out.println();
    }

    searcher.close();
  }

  static void suggest(String indexPath, String query) throws Exception {
    Suggester suggester = new Suggester(indexPath);
    List<String> suggestions = suggester.suggest(query);
    for (String text : suggestions) {
      System.out.println("Suggestion: " + text);
    }
    suggester.close();
  }
}
