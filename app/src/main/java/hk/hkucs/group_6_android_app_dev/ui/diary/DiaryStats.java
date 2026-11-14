package hk.hkucs.group_6_android_app_dev.ui.diary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Streaks and the busiest day, worked out from the heatmap's day counts. */
final class DiaryStats {

    /** Longest run of consecutive days with at least one record. */
    final int longestStreak;
    /** Run of days with records ending today, or yesterday when today has none yet. */
    final int currentStreak;
    /** Day with the most records ({@code YYYY-MM-DD}); the latest one on a tie. Null if none. */
    @Nullable
    final String busiestDate;
    final int busiestCount;

    private DiaryStats(int longestStreak, int currentStreak, @Nullable String busiestDate,
            int busiestCount) {
        this.longestStreak = longestStreak;
        this.currentStreak = currentStreak;
        this.busiestDate = busiestDate;
        this.busiestCount = busiestCount;
    }

    @NonNull
    static DiaryStats from(@Nullable Map<String, Integer> counts, @NonNull Calendar today) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        List<String> days = new ArrayList<>();
        String busiest = null;
        int busiestCount = 0;
        if (counts != null) {
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                int count = entry.getValue() == null ? 0 : entry.getValue();
                if (count <= 0) {
                    continue;
                }
                days.add(entry.getKey());
                if (count > busiestCount
                        || (count == busiestCount && entry.getKey().compareTo(busiest) > 0)) {
                    busiest = entry.getKey();
                    busiestCount = count;
                }
            }
        }
        Collections.sort(days);

        int longest = 0;
        int run = 0;
        String previous = null;
        for (String day : days) {
            run = previous != null && day.equals(next(format, previous)) ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = day;
        }

        Calendar cursor = (Calendar) today.clone();
        if (!days.contains(format.format(cursor.getTime()))) {
            cursor.add(Calendar.DAY_OF_MONTH, -1);
        }
        int current = 0;
        while (days.contains(format.format(cursor.getTime()))) {
            current++;
            cursor.add(Calendar.DAY_OF_MONTH, -1);
        }
        return new DiaryStats(longest, current, busiest, busiestCount);
    }

    @Nullable
    private static String next(SimpleDateFormat format, String day) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(format.parse(day));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return format.format(calendar.getTime());
        } catch (ParseException e) {
            return null;
        }
    }
}
