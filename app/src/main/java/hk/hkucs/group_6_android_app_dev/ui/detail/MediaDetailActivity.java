package hk.hkucs.group_6_android_app_dev.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
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

import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.ui.edit.EditMediaActivity;
import hk.hkucs.group_6_android_app_dev.util.MediaMeta;
import hk.hkucs.group_6_android_app_dev.util.StatusBadge;

/** Title details: change status and rating, see records, edit and delete. */
public class MediaDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MEDIA_ID =
            "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

    private MediaDetailViewModel vm;
    private ChipGroup statusGroup;
    private RatingBar ratingBar;
    private TextView ratingValue;
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
        ratingBar = findViewById(R.id.detail_rating_bar);
        ratingValue = findViewById(R.id.detail_rating_value);

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
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser || updatingUi) {
                return;
            }
            vm.setRating(rating <= 0f ? null : Math.min(rating * 2f, 10f));
            updateRatingLabel(rating);
        });
        findViewById(R.id.detail_clear_rating).setOnClickListener(v -> {
            updatingUi = true;
            ratingBar.setRating(0f);
            updatingUi = false;
            updateRatingLabel(0f);
            vm.setRating(null);
        });
        findViewById(R.id.detail_edit).setOnClickListener(v -> {
            MediaItem item = vm.getItem().getValue();
            if (item != null) {
                startActivity(EditMediaActivity.newIntent(this, item.id));
            }
        });
        findViewById(R.id.detail_delete).setOnClickListener(v -> confirmDelete());

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

        updatingUi = true;
        for (int i = 0; i < statusGroup.getChildCount(); i++) {
            View child = statusGroup.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(item.status.equals(child.getTag()));
            }
        }
        ratingBar.setRating(item.rating == null ? 0f : item.rating / 2f);
        updatingUi = false;
        updateRatingLabel(item.rating == null ? 0f : item.rating);
    }

    /** Takes the 0–10 score (0 means not rated). */
    private void updateRatingLabel(float score) {
        if (score <= 0f) {
            ratingValue.setText(R.string.not_rated);
            ratingBar.setContentDescription(null);
        } else {
            String text = MediaMeta.score(score);
            ratingValue.setText(getString(R.string.rating_value, text));
            ratingBar.setContentDescription(getString(R.string.rating_description, text));
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
