package hk.hkucs.group_6_android_app_dev.util;

import android.content.res.Resources;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hk.hkucs.group_6_android_app_dev.R;

/** English display text for watch record dates, episodes and durations. */
public final class RecordFormat {

    private RecordFormat() {
    }

    /** {@code 2026-11-06} as {@code Fri, Nov 6, 2026}; anything unparsable is returned as is. */
    @NonNull
    public static String longDate(@NonNull String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate);
            return new SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(date);
        } catch (ParseException | NullPointerException e) {
            return isoDate;
        }
    }

    /** "Episode 3", "Episodes 1–6" for a consecutive run, otherwise "Episodes 1, 3, 5". */
    @NonNull
    public static String episodes(@NonNull Resources res, @NonNull List<Integer> episodes) {
        if (episodes.size() == 1) {
            return res.getString(R.string.diary_episode_one, episodes.get(0));
        }
        List<Integer> sorted = new ArrayList<>(episodes);
        Collections.sort(sorted);
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i) != sorted.get(i - 1) + 1) {
                return res.getString(R.string.diary_episode_list, TextUtils.join(", ", sorted));
            }
        }
        return res.getString(R.string.diary_episode_range, sorted.get(0),
                sorted.get(sorted.size() - 1));
    }

    /** {@code 284} as {@code 4 h 44 min}; under an hour as {@code 45 min}. */
    @NonNull
    public static String duration(@NonNull Resources res, int minutes) {
        return minutes >= 60
                ? res.getString(R.string.profile_hours_minutes, minutes / 60, minutes % 60)
                : res.getString(R.string.profile_minutes_only, minutes);
    }
}
