package com.player.subtitles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

public interface Format {

    Map<Integer, Caption> parse(BufferedReader reader, TextFormatter formatter) throws IOException, SubtitlesException;

    void write(BufferedWriter writer, Map<Integer, Caption> captions) throws IOException;
}