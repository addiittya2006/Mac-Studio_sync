package se.popcorn_time.base.parser;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import se.popcorn_time.base.model.video.info.Episode;
import se.popcorn_time.base.model.video.info.MoviesInfo;
import se.popcorn_time.base.model.video.info.Torrent;
import se.popcorn_time.base.model.video.info.TvShowsInfo;
import se.popcorn_time.base.model.video.info.VideoInfo;
import se.popcorn_time.base.utils.JSONHelper;

public final class ApiParser {

    public static final String VIDEO_TITLE = "title";
    public static final String VIDEO_YEAR = "year";
    public static final String VIDEO_RATING = "rating";
    public static final String VIDEO_IMDB = "imdb";
    public static final String VIDEO_ACTORS = "actors";
    public static final String VIDEO_TRAILER = "trailer";
    public static final String VIDEO_DESCRIPTION = "description";
    public static final String VIDEO_POSTER_MED = "poster_med";
    public static final String VIDEO_POSTER_BIG = "poster_big";

    public static final String TORRENT_URL = "torrent_url";
    public static final String TORRENT_MAGNET = "torrent_magnet";
    public static final String TORRENT_SEEDS = "torrent_seeds";
    public static final String TORRENT_PEERS = "torrent_peers";
    public static final String TORRENT_FILE = "file";
    public static final String TORRENT_QUALITY = "quality";
    public static final String TORRENT_SIZE = "size_bytes";

    public static void populate(MoviesInfo info, JSONObject jsonObject) throws JSONException {
        if (info == null) {
            return;
        }
        populateVideo(info, jsonObject);
        populateTorrents(info, jsonObject.getJSONArray("items"));
    }

    public static void populate(TvShowsInfo info, JSONObject jsonObject) throws JSONException {
        if (info == null) {
            return;
        }
        populateVideo(info, jsonObject);
    }

    public static void populateTorrents(MoviesInfo info, JSONArray jsonArray) throws JSONException {
        populateTorrents(info.torrents, jsonArray);
        info.setTorrentPosition(0);
    }

    public static void populateTorrents(TvShowsInfo info, JSONObject jsonObject) throws JSONException {
        if (info == null) {
            return;
        }
        Iterator<String> iterator = jsonObject.keys();
        ArrayList<String> seasonKeys = new ArrayList<>();
        while (iterator.hasNext()) {
            seasonKeys.add(iterator.next());
        }
        Collections.sort(seasonKeys, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return Integer.parseInt(s1) - Integer.parseInt(s2);
            }
        });

        for (String seasonKey : seasonKeys) {
            JSONArray jsonSeasons = jsonObject.getJSONArray(seasonKey);
            for (int i = 0; i < jsonSeasons.length(); i++) {
                JSONObject jsonEpisode = jsonSeasons.getJSONObject(i);
                int numSeason = JSONHelper.getInt(jsonEpisode, "season", -1);
                int numEpisode = JSONHelper.getInt(jsonEpisode, "episode", -1);
                if (numSeason != -1 && numEpisode != -1) {
                    Episode episode = info.getEpisode(info.getSeason(numSeason), numEpisode);
                    episode.title = jsonEpisode.getString("title");
                    episode.description = jsonEpisode.getString("synopsis");
                    String runTime = JSONHelper.getString(jsonEpisode, "run_time", "");
                    if (!TextUtils.isEmpty(runTime)) {
                        episode.description += " <b>" + runTime + "</b>";
                    }
                    if (!jsonEpisode.isNull("items")) {
                        populateTorrents(episode.torrents, jsonEpisode.getJSONArray("items"));
                    }
                }
            }
        }

        info.setSeasonPosition(0);
        info.setEpisodePosition(0);
        info.setTorrentPosition(0);
    }

    private static void populateVideo(VideoInfo info, JSONObject jsonObject) throws JSONException {
        if (info == null) {
            return;
        }
        info.title = jsonObject.getString(VIDEO_TITLE);
        info.year = jsonObject.getString(VIDEO_YEAR);
        info.rating = (float) jsonObject.getDouble(VIDEO_RATING);
        info.imdb = jsonObject.getString(VIDEO_IMDB);
        info.actors = jsonObject.getString(VIDEO_ACTORS);
        String trailerID = jsonObject.getString(VIDEO_TRAILER);
        if (!TextUtils.isEmpty(trailerID)) {
            info.trailer = "http://www.youtube.com/embed/" + trailerID + "?autoplay=1";
        }
        info.description = jsonObject.getString(VIDEO_DESCRIPTION);
        info.posterMediumUrl = jsonObject.getString(VIDEO_POSTER_MED);
        info.posterBigUrl = jsonObject.getString(VIDEO_POSTER_BIG);
    }

    private static void populateTorrents(List<Torrent> torrents, JSONArray jsonArray) throws JSONException {
        if (torrents == null) {
            return;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonTorrent = jsonArray.getJSONObject(i);
            Torrent torrent = new Torrent();
            torrent.url = JSONHelper.getString(jsonTorrent, TORRENT_URL, null);
            torrent.magnet = JSONHelper.getString(jsonTorrent, TORRENT_MAGNET, null);
            torrent.seeds = JSONHelper.getInt(jsonTorrent, TORRENT_SEEDS, 0);
            torrent.peers = JSONHelper.getInt(jsonTorrent, TORRENT_PEERS, 0);
            torrent.file = JSONHelper.getString(jsonTorrent, TORRENT_FILE, null);
            torrent.quality = JSONHelper.getString(jsonTorrent, TORRENT_QUALITY, null);
            torrent.size = JSONHelper.getLong(jsonTorrent, TORRENT_SIZE, 0);
            torrents.add(torrent);
        }
    }
}