package hk.hkucs.group_6_android_app_dev.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;
import hk.hkucs.group_6_android_app_dev.ui.edit.EditMediaActivity;
import hk.hkucs.group_6_android_app_dev.ui.record.AddRecordActivity;
import hk.hkucs.group_6_android_app_dev.util.MediaMeta;
import hk.hkucs.group_6_android_app_dev.util.PosterLoader;
import hk.hkucs.group_6_android_app_dev.util.RecordFormat;
import hk.hkucs.group_6_android_app_dev.util.StatusBadge;

/** Title details: change status and rating, see records, edit and delete. */
public class MediaDetailActivity extends AppCompatActivity {

    public static final String EXTRA_MEDIA_ID =
            "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

    /** How long "Undo" stays on screen after deleting an entry. */
    private static final int UNDO_MS = 5000;

    private MediaDetailViewModel vm;
    private ChipGroup statusGroup;
    private TextInputLayout ratingLayout;
    private TextInputEditText ratingField;
    /** True while the UI mirrors stored data, so listeners ignore programmatic changes. */
    private boolean updatingUi;
    private boolean hadItem;
    /** Distinct episodes logged so far, for the series progress bar. */
    private int watchedEpisodes;
    private boolean progressAnimated;

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
        toolbar.inflateMenu(R.menu.menu_detail);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_share) {
                shareTitle();
                return true;
            }
            return false;
        });

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
        renderProgress(item);
    }

    /** Series only: how many distinct episodes have been logged out of the total. */
    private void renderProgress(@Nullable MediaItem item) {
        View box = findViewById(R.id.detail_progress_box);
        boolean show = item != null && MediaType.SERIES.equals(item.type)
                && item.totalEpisodes != null && item.totalEpisodes > 0;
        box.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            return;
        }
        int total = item.totalEpisodes;
        int watched = Math.min(watchedEpisodes, total);
        ((TextView) findViewById(R.id.detail_progress_label)).setText(
                getString(R.string.detail_progress, watched, total));
        LinearProgressIndicator bar = findViewById(R.id.detail_progress);
        bar.setProgressCompat(Math.round(1000f * watched / total), !progressAnimated);
        progressAnimated = true;
    }

    private void shareTitle() {
        MediaItem item = vm.getItem().getValue();
        if (item == null) {
            return;
        }
        String name = item.year == null ? item.title
                : getString(R.string.share_title_year, item.title, item.year);
        String text = item.rating == null ? getString(R.string.share_title_unrated, name)
                : getString(R.string.share_title_rated, name, MediaMeta.score(item.rating));
        share(text + " " + getString(R.string.share_signature));
    }

    private void shareRecord(WatchRecord record) {
        MediaItem item = vm.getItem().getValue();
        if (item == null) {
            return;
        }
        StringBuilder text = new StringBuilder(getString(R.string.share_record, item.title,
                RecordFormat.longDate(record.date)));
        if (record.rating != null) {
            text.append(' ').append(getString(R.string.share_record_rating,
                    MediaMeta.score(record.rating)));
        }
        if (!record.note.trim().isEmpty()) {
            text.append(' ').append(getString(R.string.share_record_note, record.note.trim()));
        }
        text.append(' ').append(getString(R.string.share_signature));
        share(text.toString());
    }

    private void share(String text) {
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
    }

    private void showRecordMenu(View anchor, WatchRecord record) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.inflate(R.menu.menu_record);
        menu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.action_share_record) {
                shareRecord(record);
                return true;
            }
            if (menuItem.getItemId() == R.id.action_delete_record) {
                vm.deleteRecord(record);
                Snackbar.make(findViewById(R.id.detail_root), R.string.record_deleted,
                                UNDO_MS)
                        .setAction(R.string.undo, v -> vm.restoreRecord(record))
                        .show();
                return true;
            }
            return false;
        });
        menu.show();
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
        renderRecordSummary(records);
        Set<Integer> episodes = new HashSet<>();
        if (records != null) {
            for (WatchRecord record : records) {
                episodes.addAll(record.episodes);
            }
        }
        watchedEpisodes = episodes.size();
        renderProgress(vm.getItem().getValue());
        if (empty) {
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        for (WatchRecord record : records) {
            View view = inflater.inflate(R.layout.item_record, container, false);
            ((TextView) view.findViewById(R.id.record_date)).setText(
                    RecordFormat.longDate(record.date));
            view.findViewById(R.id.record_more).setOnClickListener(
                    anchor -> showRecordMenu(anchor, record));
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
        if (!record.episodes.isEmpty()) {
            sb.append(RecordFormat.episodes(getResources(), record.episodes));
        }
        if (record.minutes != null) {
            appendSeparator(sb);
            sb.append(RecordFormat.duration(getResources(), record.minutes));
        }
        if (record.rating != null) {
            appendSeparator(sb);
            sb.append(getString(R.string.record_rating_star, MediaMeta.score(record.rating)));
        }
        return sb.toString();
    }

    /** "3 entries · 5 h 2 min", plus the rating change when it was rated more than once. */
    private void renderRecordSummary(@Nullable List<WatchRecord> records) {
        TextView summary = findViewById(R.id.records_summary);
        if (records == null || records.isEmpty()) {
            summary.setVisibility(View.GONE);
            return;
        }
        int minutes = 0;
        Float first = null;
        Float last = null;
        int rated = 0;
        // Records are newest first: the last rated one in the list is the earliest rating.
        for (WatchRecord record : records) {
            minutes += record.minutes == null ? 0 : record.minutes;
            if (record.rating != null) {
                if (last == null) {
                    last = record.rating;
                }
                first = record.rating;
                rated++;
            }
        }
        String text = getString(R.string.records_summary,
                getResources().getQuantityString(R.plurals.profile_entries, records.size(),
                        records.size()),
                RecordFormat.duration(getResources(), minutes));
        if (rated > 1 && !first.equals(last)) {
            text = getString(R.string.records_summary, text, getString(
                    R.string.records_rating_trend, MediaMeta.score(first), MediaMeta.score(last)));
        }
        summary.setText(text);
        summary.setVisibility(View.VISIBLE);
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
