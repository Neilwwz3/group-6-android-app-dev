package hk.hkucs.group_6_android_app_dev.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.model.DayCount;
import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;

public class RepositoryTransformsTest {

    @Test
    public void countGenres_sortsByCountThenName() {
        List<GenreCount> counts = MediaRepository.countGenres(Arrays.asList(
                media("Drama", "Sci-Fi"), media("Drama"), media("Comedy", "Sci-Fi"), media()));

        assertEquals(3, counts.size());
        assertEquals("Drama", counts.get(0).genre);
        assertEquals(2, counts.get(0).count);
        assertEquals("Sci-Fi", counts.get(1).genre);
        assertEquals("Comedy", counts.get(2).genre);
        assertEquals(1, counts.get(2).count);
    }

    @Test
    public void countGenres_emptyOrNull() {
        assertTrue(MediaRepository.countGenres(null).isEmpty());
        assertTrue(MediaRepository.countGenres(Collections.emptyList()).isEmpty());
    }

    @Test
    public void toDayMap_keepsEveryDay() {
        Map<String, Integer> map = MediaRepository.toDayMap(Arrays.asList(
                day("2026-11-01", 2), day("2026-11-03", 1)));

        assertEquals(2, map.size());
        assertEquals(Integer.valueOf(2), map.get("2026-11-01"));
        assertEquals(Integer.valueOf(1), map.get("2026-11-03"));
        assertTrue(MediaRepository.toDayMap(null).isEmpty());
    }

    private static MediaItem media(String... genres) {
        MediaItem item = new MediaItem();
        item.genres = Arrays.asList(genres);
        return item;
    }

    private static DayCount day(String date, int count) {
        DayCount day = new DayCount();
        day.date = date;
        day.count = count;
        return day;
    }
}
