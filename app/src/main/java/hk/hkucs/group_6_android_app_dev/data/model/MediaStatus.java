package hk.hkucs.group_6_android_app_dev.data.model;

import androidx.annotation.StringRes;

import hk.hkucs.group_6_android_app_dev.R;

/** Values stored in {@code media_item.status}. */
public final class MediaStatus {

    public static final String WANT = "want";
    public static final String WATCHING = "watching";
    public static final String WATCHED = "watched";
    public static final String DROPPED = "dropped";

    private MediaStatus() {
    }

    /** The string resource that shows this status in the UI. */
    @StringRes
    public static int labelRes(String status) {
        if (WATCHING.equals(status)) {
            return R.string.status_watching;
        }
        if (WATCHED.equals(status)) {
            return R.string.status_watched;
        }
        if (DROPPED.equals(status)) {
            return R.string.status_dropped;
        }
        return R.string.status_want;
    }
}
