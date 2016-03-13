package com.player.subtitles;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.player.utils.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class FormatSRT implements Format {

    public static final String EXTENSION = "srt";

    final String TIME_DELIMITER = "-->";

    private StringBuilder builder = new StringBuilder();

    @Override
    public Map<Integer, Caption> parse(@NonNull BufferedReader reader, TextFormatter formatter) throws IOException, SubtitlesException {
        reader.mark(Integer.MAX_VALUE);
        if (65279 != reader.read()) {
            // not BOM
            reader.reset();
        }

        Map<Integer, Caption> captions = new TreeMap<>();
        int index = 1;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(TIME_DELIMITER)) {
                String[] times = line.split(TIME_DELIMITER);
                if (times.length == 2) {
                    try {
                        Caption caption = new Caption();
                        caption.startMillis = parseTime(times[0]);
                        caption.endMillis = parseTime(times[1]);
                        builder.setLength(0);
                        while (true) {
                            line = reader.readLine();
                            if (TextUtils.isEmpty(line)) {
                                break;
                            } else {
                                if (builder.length() > 0) {
                                    builder.append("\n");
                                }
                                if (SubtitlesUtils.isRTL(line)) {
                                    line = SubtitlesUtils.replaceRTLSymbols(line);
                                }
                                builder.append(line);
                            }
                        }
                        caption.content = formatter != null ? formatter.format(builder.toString()) : builder.toString();
                        captions.put(index, caption);
                        index++;
                    } catch (IllegalArgumentException e) {
                        Log.e("player", "FormatSRT<parse>: Error", e);
                    }
                } else {
                    throw new SubtitlesException("Wrong times count: " + index);
                }
            }
        }

        reader.close();

        return captions;
    }

    @Override
    public void write(@NonNull BufferedWriter writer, Map<Integer, Caption> captions) throws IOException {
        for (Integer key : captions.keySet()) {
            writer.write(Integer.toString(key));
            writer.newLine();

            builder.setLength(0);
            builder.append(StringUtil.millisToString(captions.get(key).startMillis, StringUtil.TIME_SRT_FORMAT));
            builder.append(" ");
            builder.append(TIME_DELIMITER);
            builder.append(" ");
            builder.append(StringUtil.millisToString(captions.get(key).endMillis, StringUtil.TIME_SRT_FORMAT));
            writer.write(builder.toString());
            writer.newLine();

            writer.write(captions.get(key).content);
            writer.newLine();
            writer.newLine();
        }
        writer.close();
    }

    private int parseTime(String time) throws IllegalArgumentException {
        // 00:00:00,000
        time = time.trim();
        if (time.length() != 12) {
            throw new IllegalArgumentException("Wrong time length: " + time.length() + ", time: " + time);
        }
        int h = Integer.parseInt(time.substring(0, 2));
        int m = Integer.parseInt(time.substring(3, 5));
        int s = Integer.parseInt(time.substring(6, 8));
        int ms = Integer.parseInt(time.substring(9, 12));
        return h * 3600000 + m * 60000 + s * 1000 + ms;
    }
}