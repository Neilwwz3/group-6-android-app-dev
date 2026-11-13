package hk.hkucs.group_6_android_app_dev.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.ui.edit.EditMediaActivity;
import hk.hkucs.group_6_android_app_dev.ui.record.AddRecordActivity;
import hk.hkucs.group_6_android_app_dev.util.MediaMeta;
import hk.hkucs.group_6_android_app_dev.util.PosterLoader;
import hk.hkucs.group_6_android_app_dev.util.StatusBadge;

/** Title details: change status and rating, see records, edit and delete. */
public class MediaDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MEDIA_ID =
            "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

    private MediaDetailViewModel vm;
    private ChipGroup statusGroup;
    private TextInputLayout ratingLayout;
    private TextInputEditText ratingField;
    /** True while the UI mirrors stored data, so listeners ignore programmatic changes. */
    private boolean updatingUi;
    private boolean hadItem;

    public static Intent newIntent(Context context, @NonNull String mediaId) {
        if (mediaId == null || mediaId.trim().isEmpty()) {
            throw new IllegalArgumentException("mediaId is required");
        }
        return new Intent(context, MediaDetailActivity.class).putExtra(EXTRA_MEDIA_ID, mediaId);
    }

    /** A nonblank String ID is required here. */
    @SuppressWarnings("deprecation")
    private boolean hasValidArguments() {
        try {
            Bundle extras = getIntent().getExtras();
            Object value = extras == null ? null : extras.get(EXTRA_MEDIA_ID);
            return value instanceof String && !((String) value).trim().isEmpty();
        } catch (RuntimeException invalidExtras) {
            return false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if (!hasValidArguments()) {
            Toast.makeText(this, R.string.invalid_navigation_arguments, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_media_detail);
        View root = findViewById(R.id.detail_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.detail_toolbar);
        toolbar.setTitle(R.string.details_title);
        toolbar.setNavigationOnClickListener(view -> finish());

        statusGroup = findViewById(R.id.detail_status_group);
        ratingLayout = findViewById(R.id.detail_rating_layout);
        ratingField = findViewById(R.id.detail_rating_input);

        statusGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (updatingUi || checkedIds.isEmpty()) {
                return;
            }
            Chip chip = group.findViewById(checkedIds.get(0));
            Object tag = chip.getTag();
            if (tag instanceof String) {
                vm.setStatus((String) tag);
            }
        });
        ratingField.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                commitRating();
            }
        });
        ratingField.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                ratingField.clearFocus();
                return true;
            }
            return false;
        });
        findViewById(R.id.detail_clear_rating).setOnClickListener(v -> {
            updatingUi = true;
            ratingField.setText(null);
            ratingLayout.setError(null);
            updatingUi = false;
            vm.setRating(null);
        });
        findViewById(R.id.detail_edit).setOnClickListener(v -> {
            MediaItem item = vm.getItem().getValue();
            if (item != null) {
                startActivity(EditMediaActivity.newIntent(this, item.id));
            }
        });
        findViewById(R.id.detail_delete).setOnClickListener(v -> confirmDelete());
        findViewById(R.id.detail_add_record).setOnClickListener(v -> {
            MediaItem item = vm.getItem().getValue();
            if (item != null) {
                startActivity(AddRecordActivity.newIntent(this, item.id));
            }
        });

        vm = new ViewModelProvider(this).get(MediaDetailViewModel.class);
        vm.setMediaId(getIntent().getStringExtra(EXTRA_MEDIA_ID));

        vm.getItem().observe(this, item -> {
            if (item == null) {
                if (hadItem) {
                    finish();
                }
                return;
            }
            hadItem = true;
            bindItem(item);
        });
        vm.getRecords().observe(this, this::renderRecords);
    }

    private void bindItem(MediaItem item) {
        TextView title = findViewById(R.id.detail_title);
        title.setText(item.title);
        TextView original = findViewById(R.id.detail_original);
        if (item.originalTitle == null || item.originalTitle.trim().isEmpty()) {
            original.setVisibility(View.GONE);
        } else {
            original.setVisibility(View.VISIBLE);
            original.setText(item.originalTitle);
        }
        ((TextView) findViewById(R.id.detail_meta)).setText(MediaMeta.line(this, item));
        StatusBadge.apply(findViewById(R.id.detail_badge), item.status);
        ((TextView) findViewById(R.id.detail_poster_letter)).setText(
                item.title.isEmpty() ? "?"
                        : item.title.substring(0, 1).toUpperCase(java.util.Locale.US));
        PosterLoader.load(this, item.poster,
                findViewById(R.id.detail_poster_image), findViewById(R.id.detail_poster_letter));

        updatingUi = true;
        for (int i = 0; i < statusGroup.getChildCount(); i++) {
            View child = statusGroup.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(item.status.equals(child.getTag()));
            }
        }
        ratingField.setText(item.rating == null ? null : MediaMeta.score(item.rating));
        ratingLayout.setError(null);
        updatingUi = false;
    }

    /** Saves the typed 0–10 rating with one-decimal precision; blank means not rated. */
    private void commitRating() {
        if (updatingUi) {
            return;
        }
        String text = ratingField.getText().toString().trim();
        if (text.isEmpty()) {
            ratingLayout.setError(null);
            vm.setRating(null);
            return;
        }
        try {
            float value = Float.parseFloat(text);
            if (value < 0f || value > 10f) {
                throw new NumberFormatException();
            }
            ratingLayout.setError(null);
            vm.setRating(Math.round(value * 10f) / 10f);
        } catch (NumberFormatException e) {
            ratingLayout.setError(getString(R.string.invalid_rating));
        }
    }

    private void renderRecords(@Nullable List<WatchRecord> records) {
        LinearLayout container = findViewById(R.id.records_container);
        container.removeAllViews();
        boolean empty = records == null || records.isEmpty();
        findViewById(R.id.records_empty).setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        for (WatchRecord record : records) {
            View view = inflater.inflate(R.layout.item_record, container, false);
            ((TextView) view.findViewById(R.id.record_date)).setText(record.date);
            TextView note = view.findViewById(R.id.record_note);
            if (record.note.trim().isEmpty()) {
                note.setVisibility(View.GONE);
            } else {
                note.setText(record.note);
            }
            ((TextView) view.findViewById(R.id.record_meta)).setText(recordMeta(record));
            container.addView(view);
        }
    }

    private String recordMeta(WatchRecord record) {
        StringBuilder sb = new StringBuilder();
        if (record.minutes != null) {
            sb.append(getString(R.string.record_minutes, record.minutes));
        }
        if (!record.episodes.isEmpty()) {
            appendSeparator(sb);
            StringBuilder episodes = new StringBuilder();
            for (Integer episode : record.episodes) {
                if (episodes.length() > 0) {
                    episodes.append(", ");
                }
                episodes.append(episode);
            }
            sb.append(getString(R.string.record_episodes, episodes));
        }
        if (record.rating != null) {
            appendSeparator(sb);
            sb.append(getString(R.string.rating_value, MediaMeta.score(record.rating)));
        }
        return sb.toString();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> vm.delete(this::finish))
                .show();
    }

    private static void appendSeparator(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.append(" · ");
        }
    }
}
