package se.popcorn_time.base.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {

    public static String getString(JSONObject obj, String key, String defaultValue) {
        if (obj != null && obj.has(key)) {
            try {
                String str = obj.getString(key);
                if ("null".equals(str)) {
                    str = null;
                }
                return str;
            } catch (JSONException e) {
                Logger.error("JSONHelper<getString>: Error", e);
            }
        }
        return defaultValue;
    }

    public static double getDouble(JSONObject obj, String key, double defaultValue) {
        if (obj != null && obj.has(key)) {
            try {
                return obj.getDouble(key);
            } catch (JSONException e) {
                Logger.error("JSONHelper<getDouble>: Error", e);
            }
        }
        return defaultValue;
    }

    public static int getInt(JSONObject obj, String key, int defaultValue) {
        if (obj != null && obj.has(key)) {
            try {
                return obj.getInt(key);
            } catch (JSONException e) {
                Logger.error("JSONHelper<getInt>: Error", e);
            }
        }
        return defaultValue;
    }

    public static long getLong(JSONObject obj, String key, long defaultValue) {
        if (obj != null && obj.has(key)) {
            try {
                return obj.getLong(key);
            } catch (JSONException e) {
                Logger.error("JSONHelper<getLong>: Error", e);
            }
        }
        return defaultValue;
    }
}