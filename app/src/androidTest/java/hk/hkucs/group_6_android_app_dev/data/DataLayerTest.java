package hk.hkucs.group_6_android_app_dev.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.db.AppDatabase;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.model.DayCount;
import hk.hkucs.group_6_android_app_dev.data.model.MediaStatus;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;
import hk.hkucs.group_6_android_app_dev.data.repository.SampleDataLoader;

@RunWith(AndroidJUnit4.class)
public class DataLayerTest {

    private AppDatabase db;
    private MediaRepository repo;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        repo = new MediaRepository(db, Runnable::run);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void saveMedia_assignsIdAndKeepsListsAndCreatedTime() {
        MediaItem item = media(MediaType.MOVIE, MediaStatus.WANT);
        item.genres = Arrays.asList("Drama", "Sci-Fi");
        String id = repo.saveMediaNow(item);

        MediaItem saved = db.mediaDao().getById(id);
        assertFalse(id.isEmpty());
        assertEquals(Arrays.asList("Drama", "Sci-Fi"), saved.genres);

        long created = saved.createdAt;
        saved.title = "Renamed";
        saved.createdAt = 0;
        repo.saveMediaNow(saved);
        assertEquals(created, db.mediaDao().getById(id).createdAt);
        assertEquals("Renamed", db.mediaDao().getById(id).title);
    }

    @Test
    public void updatingMedia_keepsItsRecords() {
        String id = repo.saveMediaNow(media(MediaType.MOVIE, MediaStatus.WATCHED));
        repo.addRecordNow(record(id, "2026-11-01"));

        MediaItem item = db.mediaDao().getById(id);
        item.title = "Edited";
        repo.saveMediaNow(item);

        assertEquals(1, db.watchRecordDao().getDailyCounts("2026-01-01", "2026-12-31").size());
    }

    @Test
    public void addRecord_movesWantedMovieToWatched_andSeriesToWatching() {
        String movie = repo.saveMediaNow(media(MediaType.MOVIE, MediaStatus.WANT));
        String series = repo.saveMediaNow(media(MediaType.SERIES, MediaStatus.WANT));
        String dropped = repo.saveMediaNow(media(MediaType.SERIES, MediaStatus.DROPPED));

        repo.addRecordNow(record(movie, "2026-11-01"));
        repo.addRecordNow(record(series, "2026-11-01"));
        repo.addRecordNow(record(dropped, "2026-11-01"));

        assertEquals(MediaStatus.WATCHED, db.mediaDao().getById(movie).status);
        assertEquals(MediaStatus.WATCHING, db.mediaDao().getById(series).status);
        assertEquals(MediaStatus.DROPPED, db.mediaDao().getById(dropped).status);
    }

    @Test
    public void deleteRecord_thenRestore_putsTheSameRecordBackWithoutChangingStatus() {
        String id = repo.saveMediaNow(media(MediaType.MOVIE, MediaStatus.WANT));
        repo.addRecordNow(record(id, "2026-11-01"));
        repo.updateStatus(id, MediaStatus.DROPPED);
        List<DayCount> before = db.watchRecordDao().getDailyCounts("2026-01-01", "2026-12-31");
        assertEquals(1, before.size());

        WatchRecord saved = new WatchRecord();
        saved.mediaId = id;
        saved.date = "2026-11-01";
        long recordId = db.watchRecordDao().insert(saved);
        saved.id = recordId;
        repo.deleteRecord(recordId, null);
        assertEquals(1, db.watchRecordDao()
                .getDailyCounts("2026-11-01", "2026-11-01").get(0).count);

        repo.restoreRecord(saved, null);
        assertEquals(2, db.watchRecordDao()
                .getDailyCounts("2026-11-01", "2026-11-01").get(0).count);
        assertEquals(MediaStatus.DROPPED, db.mediaDao().getById(id).status);
    }

    @Test
    public void dailyCounts_groupByDayWithinRangeInclusive() {
        String id = repo.saveMediaNow(media(MediaType.MOVIE, MediaStatus.WATCHED));
        repo.addRecordNow(record(id, "2025-11-30"));
        repo.addRecordNow(record(id, "2025-12-01"));
        repo.addRecordNow(record(id, "2025-12-01"));
        repo.addRecordNow(record(id, "2026-11-30"));
        repo.addRecordNow(record(id, "2026-12-01"));

        List<DayCount> days = db.watchRecordDao().getDailyCounts("2025-12-01", "2026-11-30");

        assertEquals(2, days.size());
        int total = 0;
        for (DayCount day : days) {
            total += day.count;
        }
        assertEquals(3, total);
    }

    @Test
    public void clearAll_removesEverything() {
        String id = repo.saveMediaNow(media(MediaType.MOVIE, MediaStatus.WATCHED));
        repo.addRecordNow(record(id, "2026-11-01"));

        repo.clearAllNow();

        assertNull(db.mediaDao().getById(id));
        assertTrue(db.watchRecordDao().getDailyCounts("2000-01-01", "2100-01-01").isEmpty());
    }

    @Test
    public void sampleParser_readsTheD1Format() throws Exception {
        String json = "{\"version\":1,\"items\":[{\"id\":\"interstellar-2014\",\"title\":\"Interstellar\","
                + "\"year\":2014,\"type\":\"movie\",\"genres\":[\"Sci-Fi\",\"Drama\"],\"directors\":[],"
                + "\"cast\":[],\"overview\":\"x\",\"runtimeMinutes\":169,\"totalEpisodes\":null,"
                + "\"poster\":null,\"status\":\"watched\",\"rating\":9.5,\"tags\":[],\"records\":["
                + "{\"date\":\"2026-03-14\",\"episodes\":null,\"minutes\":169,\"rating\":9.0,\"note\":\"a\"}]},"
                + "{\"id\":\"show-2020\",\"title\":\"Show\",\"type\":\"series\",\"status\":\"bogus\","
                + "\"totalEpisodes\":10,\"records\":[{\"date\":\"2026-04-01\",\"episodes\":[1,2]}]}]}";

        SampleDataLoader.Parsed parsed = SampleDataLoader.parse(json, 1_000_000L);

        assertEquals(2, parsed.items.size());
        assertEquals(2, parsed.records.size());
        MediaItem movie = parsed.items.get(0);
        assertEquals(Float.valueOf(9.5f), movie.rating);
        assertNull(movie.poster);
        assertNull(movie.totalEpisodes);
        assertEquals(MediaStatus.WANT, parsed.items.get(1).status);
        assertEquals(Arrays.asList(1, 2), parsed.records.get(1).episodes);
        assertTrue(parsed.items.get(0).updatedAt > parsed.items.get(1).updatedAt);

        db.mediaDao().insertAll(parsed.items);
        db.watchRecordDao().insertAll(parsed.records);
        assertEquals(2, db.watchRecordDao().getDailyCounts("2026-01-01", "2026-12-31").size());
    }

    private static MediaItem media(String type, String status) {
        MediaItem item = new MediaItem();
        item.title = "Title";
        item.type = type;
        item.status = status;
        return item;
    }

    private static WatchRecord record(String mediaId, String date) {
        WatchRecord record = new WatchRecord();
        record.mediaId = mediaId;
        record.date = date;
        return record;
    }
}
