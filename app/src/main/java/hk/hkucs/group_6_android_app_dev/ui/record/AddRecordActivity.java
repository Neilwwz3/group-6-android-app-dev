package hk.hkucs.group_6_android_app_dev.ui.record;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.ui.components.FeaturePlaceholderActivity;

/** G1 entry contract for the future G3 implementation. */
public class AddRecordActivity extends FeaturePlaceholderActivity {
    public static final String EXTRA_MEDIA_ID = "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

    public static Intent newIntent(Context context, @NonNull String mediaId) {
        if (mediaId == null || mediaId.trim().isEmpty()) { throw new IllegalArgumentException("mediaId is required"); }
        return new Intent(context, AddRecordActivity.class).putExtra(EXTRA_MEDIA_ID, mediaId);
    }
    @Override protected boolean hasValidArguments() { return hasValidMediaId(EXTRA_MEDIA_ID, true); }
    @Override protected int titleResource() { return R.string.record_title; }
    @Override protected int messageResource() { return R.string.record_placeholder; }
}
