package hk.hkucs.group_6_android_app_dev.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import hk.hkucs.group_6_android_app_dev.data.db.AppDatabase;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.model.DayCount;
import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;
import hk.hkucs.group_6_android_app_dev.data.model.MediaStatus;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;
import hk.hkucs.group_6_android_app_dev.data.model.MonthTotal;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;

/**
 * The only way pages read and write data. See docs/data-api.md.
 *
 * <p>{@code observe*} methods return LiveData that refreshes when the data changes. Writes run on
 * a single background thread in call order; their callbacks run on the main thread.
 */
public class MediaRepository {

    private static volatile MediaRepository instance;

    private final AppDatabase db;
    private final Executor writer;
    private final Handler main = new Handler(Looper.getMainLooper());

    @VisibleForTesting
    public MediaRepository(AppDatabase db, Executor writer) {
        this.db = db;
        this.writer = writer;
    }

    public static MediaRepository get(Context context) {
        if (instance == null) {
            synchronized (MediaRepository.class) {
                if (instance == null) {
                    instance = new MediaRepository(AppDatabase.get(context),
                            Executors.newSingleThreadExecutor());
                }
            }
        }
        return instance;
    }

    // ---- Media items ----

    public LiveData<List<MediaItem>> observeAllMedia() {
        return db.mediaDao().observeAll();
    }

    public LiveData<MediaItem> observeMedia(String id) {
        return db.mediaDao().observeById(id);
    }

    public LiveData<List<MediaItem>> observeMediaByStatus(String status) {
        return db.mediaDao().observeByStatus(status);
    }

    /** Creates the item when its id is empty, otherwise updates it. Calls back with its id. */
    public void saveMedia(@NonNull MediaItem item, @Nullable Consumer<String> onSaved) {
        writer.execute(() -> {
            String id = saveMediaNow(item);
            post(onSaved == null ? null : () -> onSaved.accept(id));
        });
    }

    public void updateStatus(String id, String status) {
        writer.execute(() -> db.mediaDao().updateStatus(id, status, now()));
    }

    public void updateRating(String id, @Nullable Float rating) {
        writer.execute(() -> db.mediaDao().updateRating(id, rating, now()));
    }

    /** Deletes the item and all of its watch records. */
    public void deleteMedia(String id, @Nullable Runnable onDone) {
        writer.execute(() -> {
            db.runInTransaction(() -> {
                db.watchRecordDao().deleteForMedia(id);
                db.mediaDao().deleteById(id);
            });
            post(onDone);
        });
    }

    // ---- Watch records ----

    public LiveData<List<WatchRecord>> observeRecords(String mediaId) {
        return db.watchRecordDao().observeForMedia(mediaId);
    }

    public LiveData<List<RecordWithMedia>> observeRecentRecords(int limit) {
        return db.watchRecordDao().observeRecent(limit);
    }

    public LiveData<List<RecordWithMedia>> observeTimeline() {
        return db.watchRecordDao().observeTimeline();
    }

    /** Dates ({@code YYYY-MM-DD}, inclusive) mapped to that day's record count. Days with no
     * records are absent. */
    public LiveData<Map<String, Integer>> observeDailyCounts(String fromDate, String toDate) {
        return Transformations.map(db.watchRecordDao().observeDailyCounts(fromDate, toDate),
                MediaRepository::toDayMap);
    }

    /**
     * Saves the record. A "want" item becomes "watched" (movie) or "watching" (series), since
     * the user has now started it.
     */
    public void addRecord(@NonNull WatchRecord record, @Nullable Runnable onDone) {
        writer.execute(() -> {
            addRecordNow(record);
            post(onDone);
        });
    }

    // ---- Statistics ----

    public LiveData<StatsSummary> observeSummary() {
        return db.mediaDao().observeSummary();
    }

    /** Totals for one month's records, e.g. {@code "2026-11"}. */
    public LiveData<StatsSummary> observeMonthSummary(String yearMonth) {
        return db.watchRecordDao().observeMonthSummary(yearMonth);
    }

    /** Record count and minutes per month ({@code YYYY-MM}), oldest first, for records on or
     * after {@code fromDate}. Months without records are absent. */
    public LiveData<List<MonthTotal>> observeMonthlyTotals(String fromDate) {
        return db.watchRecordDao().observeMonthlyTotals(fromDate);
    }

    /** Genres by number of media items, most common first. */
    public LiveData<List<GenreCount>> observeGenreCounts() {
        return Transformations.map(db.mediaDao().observeAll(), MediaRepository::countGenres);
    }

    // ---- Whole database ----

    /** Removes every media item and watch record. */
    public void clearAll(@Nullable Runnable onDone) {
        writer.execute(() -> {
            clearAllNow();
            post(onDone);
        });
    }

    /**
     * Replaces all data with the given items and records, on the writer thread. Used by
     * {@link SampleDataLoader}; {@code load} parses on the same thread so a bad file never
     * clears the existing data.
     */
    void replaceAll(@NonNull Loader loader, @Nullable Runnable onDone,
            @Nullable Consumer<Exception> onError) {
        writer.execute(() -> {
            try {
                SampleDataLoader.Parsed parsed = loader.load();
                db.runInTransaction(() -> {
                    clearAllNow();
                    db.mediaDao().insertAll(parsed.items);
                    db.watchRecordDao().insertAll(parsed.records);
                });
                post(onDone);
            } catch (Exception e) {
                post(onError == null ? null : () -> onError.accept(e));
            }
        });
    }

    interface Loader {
        SampleDataLoader.Parsed load() throws Exception;
    }

    // ---- Synchronous helpers (writer thread or tests only) ----

    @VisibleForTesting
    public String saveMediaNow(@NonNull MediaItem item) {
        long now = now();
        if (item.id.trim().isEmpty()) {
            item.id = UUID.randomUUID().toString();
            item.createdAt = now;
        } else if (item.createdAt == 0) {
            MediaItem existing = db.mediaDao().getById(item.id);
            item.createdAt = existing == null ? now : existing.createdAt;
        }
        item.updatedAt = now;
        db.mediaDao().upsert(item);
        return item.id;
    }

    @VisibleForTesting
    public void addRecordNow(@NonNull WatchRecord record) {
        db.runInTransaction(() -> {
            if (record.createdAt == 0) {
                record.createdAt = now();
            }
            db.watchRecordDao().insert(record);
            MediaItem media = db.mediaDao().getById(record.mediaId);
            if (media != null && MediaStatus.WANT.equals(media.status)) {
                String next = MediaType.SERIES.equals(media.type)
                        ? MediaStatus.WATCHING : MediaStatus.WATCHED;
                db.mediaDao().updateStatus(media.id, next, now());
            }
        });
    }

    @VisibleForTesting
    public void clearAllNow() {
        db.runInTransaction(() -> {
            db.watchRecordDao().deleteAll();
            db.mediaDao().deleteAll();
        });
    }

    // ---- Pure transformations, unit tested ----

    @VisibleForTesting
    public static Map<String, Integer> toDayMap(List<DayCount> days) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (days != null) {
            for (DayCount day : days) {
                map.put(day.date, day.count);
            }
        }
        return map;
    }

    @VisibleForTesting
    public static List<GenreCount> countGenres(List<MediaItem> items) {
        Map<String, Integer> counts = new HashMap<>();
        if (items != null) {
            for (MediaItem item : items) {
                for (String genre : item.genres) {
                    Integer old = counts.get(genre);
                    counts.put(genre, old == null ? 1 : old + 1);
                }
            }
        }
        List<GenreCount> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            result.add(new GenreCount(entry.getKey(), entry.getValue()));
        }
        Collections.sort(result, (a, b) -> a.count != b.count
                ? Integer.compare(b.count, a.count) : a.genre.compareTo(b.genre));
        return result;
    }

    private void post(@Nullable Runnable callback) {
        if (callback != null) {
            main.post(callback);
        }
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
