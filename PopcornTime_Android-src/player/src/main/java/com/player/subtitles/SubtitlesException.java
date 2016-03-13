package com.player.subtitles;

public class SubtitlesException extends Exception {

    private static final long serialVersionUID = -5010956658944275477L;

    public SubtitlesException(String detailMessage) {
        super(detailMessage);
    }

    public SubtitlesException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}