package hk.hkucs.group_6_android_app_dev.data.db;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/** Stores list fields as JSON arrays in a single TEXT column. */
public final class Converters {

    private Converters() {
    }

    @TypeConverter
    public static String fromStringList(List<String> values) {
        JSONArray array = new JSONArray();
        if (values != null) {
            for (String value : values) {
                array.put(value);
            }
        }
        return array.toString();
    }

    @TypeConverter
    public static List<String> toStringList(String json) {
        List<String> values = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return values;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                values.add(array.getString(i));
            }
        } catch (JSONException ignored) {
            // A malformed value reads as an empty list rather than crashing the page.
        }
        return values;
    }

    @TypeConverter
    public static String fromIntList(List<Integer> values) {
        JSONArray array = new JSONArray();
        if (values != null) {
            for (Integer value : values) {
                array.put(value);
            }
        }
        return array.toString();
    }

    @TypeConverter
    public static List<Integer> toIntList(String json) {
        List<Integer> values = new ArrayList<>();
        if (json == null || json.isEmpty()) {
            return values;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                values.add(array.getInt(i));
            }
        } catch (JSONException ignored) {
            // Same as above.
        }
        return values;
    }
}
