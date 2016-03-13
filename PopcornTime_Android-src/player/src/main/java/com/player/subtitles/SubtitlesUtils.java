package com.player.subtitles;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SubtitlesUtils {

    public static final String WITHOUT_SUBTITLES = "without-subtitles";
    public static final String CUSTOM_SUBTITLES = "custom-subtitles";

    private static final String UTF_8 = "UTF-8";

    public static String generateSubtitlePath(String videoPath, String extension) {
        return videoPath.substring(0, videoPath.lastIndexOf(".") + 1) + extension;
    }

    public static boolean isRTL(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
//        int firstIndex = 1;
//        int directionality = Character.getDirectionality(text.charAt(firstIndex));
//        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
        return true;
    }

    public static String replaceRTLSymbols(String text) {
        return text.replaceAll("\\.", "\u200e.")
                .replaceAll(",", "\u200e,")
                .replaceAll("\\?", "\u200e?")
                .replaceAll("!", "\u200e!")
                .replaceAll("-", "\u200e-");
    }

    public static void load(String subtitlesPath, String savePath) throws InterruptedException, SubtitlesException, IOException {
        if (TextUtils.isEmpty(subtitlesPath)) {
            throw new SubtitlesException("Empty subtitles path");
        }
        if (TextUtils.isEmpty(savePath)) {
            throw new SubtitlesException("Empty save path");
        }
        if (subtitlesPath.startsWith("http")) {
            loadUrl(subtitlesPath, savePath);
        } else if (subtitlesPath.startsWith("file")) {
            loadFile(Uri.parse(subtitlesPath).getPath(), savePath);
        } else {
            throw new SubtitlesException("Not supported subtitles path: " + subtitlesPath);
        }
    }

    private static void loadUrl(String url, String savePath) throws IOException, InterruptedException, SubtitlesException {
        URLConnection connectionSubtitle = new URL(url).openConnection();
        connectionSubtitle.connect();
        if (Thread.interrupted()) {
            throw new InterruptedException("Load subtitles interrupted");
        }
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(connectionSubtitle.getInputStream()));
        ZipEntry zi;
        while ((zi = zis.getNextEntry()) != null) {
            String[] part = zi.getName().split("\\.");
            if (part.length == 0) {
                continue;
            }
            String extension = part[part.length - 1];
            if (FormatSRT.EXTENSION.equals(extension)) {
                write(zis, savePath, new FormatSRT());
                break;
            } else {
                Log.d("player", "Not supported subtitle extension: " + extension);
            }
        }
        zis.closeEntry();
        zis.close();
    }

    private static void loadFile(String filePath, String savePath) throws IOException, SubtitlesException, InterruptedException {
        FileInputStream fis = new FileInputStream(new File(filePath));
        write(fis, savePath, new FormatSRT());
        fis.close();
    }

    private static void write(InputStream inputStream, String savePath, Format format) throws IOException, SubtitlesException, InterruptedException {
        UniversalDetector detector = new UniversalDetector(null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int count;
        byte[] buffer = new byte[1024];
        while ((count = inputStream.read(buffer)) > 0) {
            if (!detector.isDone()) {
                detector.handleData(buffer, 0, count);
            }
            baos.write(buffer, 0, count);
        }
        detector.dataEnd();

        String subtitleEncoding = detector.getDetectedCharset();
        detector.reset();
        if (subtitleEncoding == null || "".equals(subtitleEncoding)) {
            subtitleEncoding = UTF_8;
        } else if ("MACCYRILLIC".equals(subtitleEncoding)) {
            subtitleEncoding = "Windows-1256"; // for arabic
        }

        byte[] subtitle_utf_8 = new String(baos.toByteArray(), Charset.forName(subtitleEncoding)).getBytes(UTF_8);
        String subtitle = new String(subtitle_utf_8, Charset.forName(UTF_8));

        Map<Integer, Caption> subs = format.parse(new BufferedReader(new StringReader(subtitle)), new RemoveTagsFormatter());
        if (Thread.interrupted()) {
            throw new InterruptedException("Write subtitles interrupted");
        }
        File subtitlesFile = new File(savePath);
        if (!subtitlesFile.getParentFile().exists()) {
            subtitlesFile.getParentFile().mkdirs();
        }
        format.write(new BufferedWriter(new FileWriter(subtitlesFile)), subs);
    }
}