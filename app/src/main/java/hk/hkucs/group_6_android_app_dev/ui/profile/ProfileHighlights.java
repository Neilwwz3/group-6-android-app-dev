package hk.hkucs.group_6_android_app_dev.ui.profile;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;

/** The Profile page's highlight cards: top rated title, most logged title, movie/TV split. */
final class ProfileHighlights {

    @Nullable
    final MediaItem topRated;
    @Nullable
    final MediaItem mostLogged;
    final int mostLoggedCount;
    final int movieMinutes;
    final int seriesMinutes;

    private ProfileHighlights(@Nullable MediaItem topRated, @Nullable MediaItem mostLogged,
            int mostLoggedCount, int movieMinutes, int seriesMinutes) {
        this.topRated = topRated;
        this.mostLogged = mostLogged;
        this.mostLoggedCount = mostLoggedCount;
        this.movieMinutes = movieMinutes;
        this.seriesMinutes = seriesMinutes;
    }

    boolean isEmpty() {
        return topRated == null && mostLogged == null && movieMinutes + seriesMinutes == 0;
    }

    /**
     * @param media    all items, most recently updated first (ties go to the earlier item)
     * @param records  all records with their media
     */
    @VisibleForTesting
    static ProfileHighlights from(@Nullable List<MediaItem> media,
            @Nullable List<RecordWithMedia> records) {
        MediaItem topRated = null;
        Map<String, MediaItem> byId = new HashMap<>();
        if (media != null) {
            for (MediaItem item : media) {
                byId.put(item.id, item);
                if (item.rating != null
                        && (topRated == null || item.rating > topRated.rating)) {
                    topRated = item;
                }
            }
        }

        Map<String, Integer> perMedia = new HashMap<>();
        int movieMinutes = 0;
        int seriesMinutes = 0;
        MediaItem mostLogged = null;
        int mostLoggedCount = 0;
        if (records != null) {
            for (RecordWithMedia row : records) {
                int minutes = row.record.minutes == null ? 0 : row.record.minutes;
                if (MediaType.SERIES.equals(row.mediaType)) {
                    seriesMinutes += minutes;
                } else {
                    movieMinutes += minutes;
                }
                Integer old = perMedia.get(row.record.mediaId);
                perMedia.put(row.record.mediaId, old == null ? 1 : old + 1);
            }
            if (media != null) {
                for (MediaItem item : media) {
                    Integer count = perMedia.get(item.id);
                    if (count != null && count > mostLoggedCount) {
                        mostLogged = item;
                        mostLoggedCount = count;
                    }
                }
            }
        }
        return new ProfileHighlights(topRated, mostLogged, mostLoggedCount, movieMinutes,
                seriesMinutes);
    }
}
