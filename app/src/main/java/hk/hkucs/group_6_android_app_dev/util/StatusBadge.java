package hk.hkucs.group_6_android_app_dev.util;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.MediaStatus;

/** Applies the design-system badge colors for a watch status. */
public final class StatusBadge {

    private StatusBadge() {
    }

    public static void apply(TextView badge, String status) {
        int backgroundRes = R.drawable.status_want;
        int textColorRes = R.color.status_want_text;
        if (MediaStatus.WATCHING.equals(status)) {
            backgroundRes = R.drawable.status_watching;
            textColorRes = R.color.status_watching_text;
        } else if (MediaStatus.WATCHED.equals(status)) {
            backgroundRes = R.drawable.status_watched;
            textColorRes = R.color.status_watched_text;
        } else if (MediaStatus.DROPPED.equals(status)) {
            backgroundRes = R.drawable.status_dropped;
            textColorRes = R.color.status_dropped_text;
        }
        badge.setText(MediaStatus.labelRes(status));
        badge.setBackgroundResource(backgroundRes);
        badge.setTextColor(ContextCompat.getColor(badge.getContext(), textColorRes));
    }
}
