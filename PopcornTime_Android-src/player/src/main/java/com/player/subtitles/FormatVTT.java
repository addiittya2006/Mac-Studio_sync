package com.player.subtitles;

import com.player.utils.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

public class FormatVTT implements Format {

    public static final String EXTENSION = "vtt";

    final String TIME_DELIMITER = "-->";

    private StringBuilder builder = new StringBuilder();

    @Override
    public Map<Integer, Caption> parse(BufferedReader reader, TextFormatter formatter) throws IOException, SubtitlesException {
        //TODO: not implemented
        throw new SubtitlesException("VTT parse not implemented");
    }

    @Override
    public void write(BufferedWriter writer, Map<Integer, Caption> captions) throws IOException {
        writer.write("WEBVTT");
        writer.newLine();
        writer.newLine();

        for (Integer key : captions.keySet()) {
            builder.setLength(0);
            builder.append(StringUtil.millisToString(captions.get(key).startMillis, StringUtil.TIME_VTT_FORMAT));
            builder.append(" ");
            builder.append(TIME_DELIMITER);
            builder.append(" ");
            builder.append(StringUtil.millisToString(captions.get(key).endMillis, StringUtil.TIME_VTT_FORMAT));
            writer.write(builder.toString());
            writer.newLine();

            writer.write(captions.get(key).content);
            writer.newLine();
            writer.newLine();
        }
        writer.close();
    }
}