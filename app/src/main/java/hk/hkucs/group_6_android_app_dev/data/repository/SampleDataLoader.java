package hk.hkucs.group_6_android_app_dev.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.model.MediaStatus;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;

/**
 * Loads the demo library from {@code assets/sample/library.json} (format: docs/tasks/D-1).
 * Loading replaces all existing data.
 */
public final class SampleDataLoader {

    @VisibleForTesting
    static final String ASSET_PATH = "sample/library.json";

    private SampleDataLoader() {
    }

    public static void load(@NonNull Context context, @Nullable Runnable onDone) {
        load(context, onDone, null);
    }

    /** Like {@link #load(Context, Runnable)}; {@code onError} runs instead if the file is
     * missing or malformed, and the existing data is kept. */
    public static void load(@NonNull Context context, @Nullable Runnable onDone,
            @Nullable Consumer<Exception> onError) {
        Context app = context.getApplicationContext();
        MediaRepository.get(app).replaceAll(
                () -> parse(readAsset(app), System.currentTimeMillis()), onDone, onError);
    }

    /** Parsed contents of a sample file. */
    public static final class Parsed {
        public final List<MediaItem> items = new ArrayList<>();
        public final List<WatchRecord> records = new ArrayList<>();
    }

    @VisibleForTesting
    public static Parsed parse(String json, long now) throws JSONException {
        Parsed parsed = new Parsed();
        JSONArray items = new JSONObject(json).getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            JSONObject o = items.getJSONObject(i);
            MediaItem item = new MediaItem();
            item.id = o.getString("id");
            item.title = o.getString("title");
            item.originalTitle = optString(o, "originalTitle");
            item.year = optInt(o, "year");
            item.type = MediaType.SERIES.equals(o.optString("type")) ? MediaType.SERIES
                    : MediaType.MOVIE;
            item.genres = strings(o.optJSONArray("genres"));
            item.directors = strings(o.optJSONArray("directors"));
            item.cast = strings(o.optJSONArray("cast"));
            item.tags = strings(o.optJSONArray("tags"));
            item.overview = optString(o, "overview");
            item.runtimeMinutes = optInt(o, "runtimeMinutes");
            item.totalEpisodes = optInt(o, "totalEpisodes");
            item.poster = optString(o, "poster");
            item.status = status(o.optString("status"));
            item.rating = optFloat(o, "rating");
            item.source = "sample";
            // Keep the file's order as "most recently updated first".
            item.createdAt = now - i * 1000L;
            item.updatedAt = item.createdAt;
            parsed.items.add(item);

            JSONArray records = o.optJSONArray("records");
            for (int j = 0; records != null && j < records.length(); j++) {
                JSONObject r = records.getJSONObject(j);
                WatchRecord record = new WatchRecord();
                record.mediaId = item.id;
                record.date = r.getString("date");
                record.episodes = ints(r.optJSONArray("episodes"));
                record.minutes = optInt(r, "minutes");
                record.rating = optFloat(r, "rating");
                record.note = r.optString("note", "");
                record.createdAt = item.createdAt;
                parsed.records.add(record);
            }
        }
        return parsed;
    }

    private static String readAsset(Context context) throws IOException {
        try (InputStream in = context.getAssets().open(ASSET_PATH)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String status(String value) {
        if (MediaStatus.WATCHING.equals(value) || MediaStatus.WATCHED.equals(value)
                || MediaStatus.DROPPED.equals(value)) {
            return value;
        }
        return MediaStatus.WANT;
    }

    @Nullable
    private static String optString(JSONObject o, String key) {
        return o.isNull(key) ? null : o.optString(key, null);
    }

    @Nullable
    private static Integer optInt(JSONObject o, String key) {
        return o.isNull(key) || !o.has(key) ? null : o.optInt(key);
    }

    @Nullable
    private static Float optFloat(JSONObject o, String key) {
        return o.isNull(key) || !o.has(key) ? null : (float) o.optDouble(key);
    }

    private static List<String> strings(@Nullable JSONArray array) {
        List<String> values = new ArrayList<>();
        for (int i = 0; array != null && i < array.length(); i++) {
            values.add(array.optString(i));
        }
        return values;
    }

    private static List<Integer> ints(@Nullable JSONArray array) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; array != null && i < array.length(); i++) {
            values.add(array.optInt(i));
        }
        return values;
    }
}
