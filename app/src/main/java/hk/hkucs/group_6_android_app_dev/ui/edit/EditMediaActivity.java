package hk.hkucs.group_6_android_app_dev.ui.edit;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.ui.components.FeaturePlaceholderActivity;

/** G1 entry contract for the future G2 implementation. */
public class EditMediaActivity extends FeaturePlaceholderActivity {
    public static final String EXTRA_MEDIA_ID = "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

    public static Intent newIntent(Context context, @Nullable String mediaId) {
        return new Intent(context, EditMediaActivity.class).putExtra(EXTRA_MEDIA_ID, mediaId);
    }
    @Override protected int titleResource() { return getIntent().getStringExtra(EXTRA_MEDIA_ID) == null ? R.string.add_title : R.string.edit_title; }
    @Override protected int messageResource() { return getIntent().getStringExtra(EXTRA_MEDIA_ID) == null ? R.string.add_placeholder : R.string.edit_placeholder; }
}

