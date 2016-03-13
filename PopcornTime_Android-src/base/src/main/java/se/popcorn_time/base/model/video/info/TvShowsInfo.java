package se.popcorn_time.base.model.video.info;

import android.os.Parcel;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public abstract class TvShowsInfo extends VideoInfo {

    private ArrayList<Season> seasons = new ArrayList<>();
    private int seasonPosition = -1;
    private int episodePosition = -1;

    public TvShowsInfo(String videoType) {
        super(videoType);
    }

    protected TvShowsInfo(Parcel parcel) {
        super(parcel);
        parcel.readTypedList(seasons, Season.CREATOR);
        seasonPosition = parcel.readInt();
        episodePosition = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(seasons);
        dest.writeInt(seasonPosition);
        dest.writeInt(episodePosition);
    }

    @Override
    public ArrayList<Torrent> getTorrents() {
        if (getCurrentEpisode() != null) {
            return getCurrentEpisode().torrents;
        }
        return null;
    }

    /*
    * Seasons
    * */

    public Season getSeason(int num) {
        for (Season season : seasons) {
            if (num == season.getNumber()) {
                return season;
            }
        }
        Season season = new Season(num);
        seasons.add(season);
        return season;
    }

    public final ArrayList<Season> getSeasons() {
        return seasons;
    }

    @Nullable
    public final Season getCurrentSeason() {
        if (seasonPosition >= 0 && seasonPosition < getSeasons().size()) {
            return getSeasons().get(seasonPosition);
        }
        return null;
    }

    public final int getSeasonPosition() {
        return seasonPosition;
    }

    public final void setSeasonPosition(int seasonPosition) {
        if (seasonPosition >= 0 && seasonPosition < seasons.size()) {
            this.seasonPosition = seasonPosition;
        }
    }

    /*
    * Episodes
    * */

    public Episode getEpisode(Season season, int num) {
        for (Episode episode : season.episodes) {
            if (num == episode.getNumber()) {
                return episode;
            }
        }
        Episode episode = new Episode(num);
        season.episodes.add(episode);
        return episode;
    }

    @Nullable
    public final ArrayList<Episode> getEpisodes() {
        if (getCurrentSeason() != null) {
            return getCurrentSeason().episodes;
        }
        return null;
    }

    @Nullable
    public final Episode getCurrentEpisode() {
        if (getEpisodes() != null && episodePosition >= 0 && episodePosition < getEpisodes().size()) {
            return getEpisodes().get(episodePosition);
        }
        return null;
    }

    public final int getEpisodePosition() {
        return episodePosition;
    }

    public final void setEpisodePosition(int episodePosition) {
        if (getEpisodes() != null && episodePosition >= 0 && episodePosition < getEpisodes().size()) {
            this.episodePosition = episodePosition;
        }
    }
}