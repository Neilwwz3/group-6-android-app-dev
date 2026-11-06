package hk.hkucs.group_6_android_app_dev.ui.components;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.appbar.MaterialToolbar;
import hk.hkucs.group_6_android_app_dev.R;

/** Temporary G1 shell; feature owners replace this with their own content. */
public abstract class FeaturePlaceholderActivity extends AppCompatActivity {
    protected abstract @StringRes int titleResource();
    protected abstract @StringRes int messageResource();
    /** Add mode accepts an absent/null ID; other modes require a nonblank String. */
    @SuppressWarnings("deprecation")
    protected final boolean hasValidMediaId(String key, boolean required) {
        try {
            Bundle extras = getIntent().getExtras();
            Object value = extras == null ? null : extras.get(key);
            return value == null ? !required
                    : value instanceof String && !((String) value).trim().isEmpty();
        } catch (RuntimeException invalidExtras) {
            return false;
        }
    }

    protected abstract boolean hasValidArguments();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!hasValidArguments()) {
            Toast.makeText(this, R.string.invalid_navigation_arguments, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feature_placeholder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.feature_root), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        ((TextView) findViewById(R.id.feature_title)).setText(titleResource());
        ((TextView) findViewById(R.id.feature_message)).setText(messageResource());
        MaterialToolbar toolbar = findViewById(R.id.feature_toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());
    }
}
