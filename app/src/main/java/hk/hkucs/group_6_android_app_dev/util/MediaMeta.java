package hk.hkucs.group_6_android_app_dev.util;

import android.content.Context;

import java.util.List;
import java.util.Locale;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;

/** Builds the small metadata lines shown under media titles. */
public final class MediaMeta {

    private MediaMeta() {
    }

    /** Example: {@code 2026 · Movie · Drama, Comedy}. */
    public static String line(Context context, MediaItem item) {
        StringBuilder sb = new StringBuilder();
        if (item.year != null) {
            sb.append(item.year);
        }
        appendSeparator(sb);
        sb.append(typeLabel(context, item.type));
        if (!item.genres.isEmpty()) {
            appendSeparator(sb);
            sb.append(abbreviate(item.genres, 3));
        }
        return sb.toString();
    }

    public static String typeLabel(Context context, String type) {
        return context.getString(MediaType.SERIES.equals(type)
                ? R.string.type_series : R.string.type_movie);
    }

    /** Shows whole numbers without a decimal point: 8, 7.5. */
    public static String score(float value) {
        if (Math.rint(value) == value) {
            return String.valueOf((int) value);
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private static String abbreviate(List<String> values, int max) {
        StringBuilder sb = new StringBuilder();
        int count = Math.min(values.size(), max);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(values.get(i));
        }
        if (values.size() > max) {
            sb.append(", …");
        }
        return sb.toString();
    }

    private static void appendSeparator(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(" · ");
        }
    }
}
