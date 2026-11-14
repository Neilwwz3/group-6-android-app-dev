package hk.hkucs.group_6_android_app_dev.ui.diary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DiaryStatsTest {

    private static Calendar day(int month, int date) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(2026, month, date, 12, 0);
        return c;
    }

    @Test
    public void longestStreakSpansMonthEnds() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("2026-01-30", 1);
        counts.put("2026-01-31", 2);
        counts.put("2026-02-01", 1);
        counts.put("2026-02-03", 1);

        DiaryStats stats = DiaryStats.from(counts, day(Calendar.NOVEMBER, 19));

        assertEquals(3, stats.longestStreak);
        assertEquals(0, stats.currentStreak);
    }

    @Test
    public void currentStreakCountsFromYesterdayWhenTodayIsEmpty() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("2026-11-17", 1);
        counts.put("2026-11-18", 1);

        assertEquals(2, DiaryStats.from(counts, day(Calendar.NOVEMBER, 19)).currentStreak);

        counts.put("2026-11-19", 1);
        assertEquals(3, DiaryStats.from(counts, day(Calendar.NOVEMBER, 19)).currentStreak);
    }

    @Test
    public void busiestDayPrefersTheLatestOnATie() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("2026-02-22", 4);
        counts.put("2026-11-07", 4);
        counts.put("2026-11-08", 2);

        DiaryStats stats = DiaryStats.from(counts, day(Calendar.NOVEMBER, 19));

        assertEquals("2026-11-07", stats.busiestDate);
        assertEquals(4, stats.busiestCount);
    }

    @Test
    public void emptyData() {
        DiaryStats stats = DiaryStats.from(null, day(Calendar.NOVEMBER, 19));

        assertEquals(0, stats.longestStreak);
        assertEquals(0, stats.currentStreak);
        assertNull(stats.busiestDate);
        assertEquals("Feb 22", DiaryAdapter.shortDate("2026-02-22"));
    }
}
