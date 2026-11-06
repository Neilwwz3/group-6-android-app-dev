package hk.hkucs.group_6_android_app_dev.ui.detail;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.ui.components.FeaturePlaceholderActivity;

/** G1 entry contract for the future G2 implementation. */
public class MediaDetailActivity extends FeaturePlaceholderActivity {
    public static final String EXTRA_MEDIA_ID = "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

    public static Intent newIntent(Context context, @NonNull String mediaId) {
        if (mediaId == null || mediaId.trim().isEmpty()) { throw new IllegalArgumentException("mediaId is required"); }
        return new Intent(context, MediaDetailActivity.class).putExtra(EXTRA_MEDIA_ID, mediaId);
    }
    @Override protected boolean hasValidArguments() { return hasValidMediaId(EXTRA_MEDIA_ID, true); }
    @Override protected int titleResource() { return R.string.details_title; }
    @Override protected int messageResource() { return R.string.details_placeholder; }
}
