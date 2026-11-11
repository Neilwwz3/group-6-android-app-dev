package hk.hkucs.group_6_android_app_dev.ui.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.model.Genres;
import hk.hkucs.group_6_android_app_dev.data.model.MediaStatus;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;
import hk.hkucs.group_6_android_app_dev.util.MediaMeta;

/** Add or edit one title. Entry contract from G1: {@link #newIntent(Context, String)}. */
public class EditMediaActivity extends AppCompatActivity {

    public static final String EXTRA_MEDIA_ID =
            "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

    private EditMediaViewModel vm;
    private TextInputLayout titleLayout;
    private TextInputLayout yearLayout;
    private TextInputLayout runtimeLayout;
    private TextInputLayout episodesLayout;
    private TextInputEditText titleField;
    private TextInputEditText originalField;
    private TextInputEditText yearField;
    private TextInputEditText directorsField;
    private TextInputEditText castField;
    private TextInputEditText tagsField;
    private TextInputEditText overviewField;
    private TextInputEditText runtimeField;
    private TextInputEditText episodesField;
    private ChipGroup typeGroup;
    private ChipGroup genresGroup;
    private ChipGroup statusGroup;
    private RatingBar ratingBar;
    private TextView ratingValue;

    private final Set<String> selectedGenres = new LinkedHashSet<>();
    private MediaItem loaded;
    private boolean populated;
    private boolean invalidNumber;

    public static Intent newIntent(Context context, @Nullable String mediaId) {
        if (mediaId != null && mediaId.trim().isEmpty()) {
            throw new IllegalArgumentException("mediaId must be null or nonblank");
        }
        return new Intent(context, EditMediaActivity.class).putExtra(EXTRA_MEDIA_ID, mediaId);
    }

    /** Add mode accepts an absent/null ID; edit mode needs a nonblank String. */
    @SuppressWarnings("deprecation")
    private boolean hasValidArguments() {
        try {
            Bundle extras = getIntent().getExtras();
            Object value = extras == null ? null : extras.get(EXTRA_MEDIA_ID);
            return value == null
                    || (value instanceof String && !((String) value).trim().isEmpty());
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
        setContentView(R.layout.activity_edit_media);
        View root = findViewById(R.id.edit_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.edit_toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());
        String mediaId = getIntent().getStringExtra(EXTRA_MEDIA_ID);
        toolbar.setTitle(mediaId == null ? R.string.add_title : R.string.edit_title);

        titleLayout = findViewById(R.id.title_layout);
        yearLayout = findViewById(R.id.year_layout);
        runtimeLayout = findViewById(R.id.runtime_layout);
        episodesLayout = findViewById(R.id.episodes_layout);
        titleField = findViewById(R.id.edit_title);
        originalField = findViewById(R.id.edit_original_title);
        yearField = findViewById(R.id.edit_year);
        directorsField = findViewById(R.id.edit_directors);
        castField = findViewById(R.id.edit_cast);
        tagsField = findViewById(R.id.edit_tags);
        overviewField = findViewById(R.id.edit_overview);
        runtimeField = findViewById(R.id.edit_runtime);
        episodesField = findViewById(R.id.edit_episodes);
        typeGroup = findViewById(R.id.type_group);
        genresGroup = findViewById(R.id.genres_group);
        statusGroup = findViewById(R.id.status_group);
        ratingBar = findViewById(R.id.edit_rating_bar);
        ratingValue = findViewById(R.id.edit_rating_value);

        buildGenreChips();
        typeGroup.setOnCheckedStateChangeListener((group, ids) -> updateEpisodesVisibility());
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                updateRatingLabel();
            }
        });
        findViewById(R.id.clear_rating).setOnClickListener(v -> {
            ratingBar.setRating(0f);
            updateRatingLabel();
        });
        findViewById(R.id.save_button).setOnClickListener(v -> save());

        vm = new ViewModelProvider(this).get(EditMediaViewModel.class);
        vm.setMediaId(mediaId);
        vm.getItem().observe(this, item -> {
            loaded = item;
            if (item != null && !populated) {
                populated = true;
                fillForm(item);
            }
        });
    }

    private void buildGenreChips() {
        LayoutInflater inflater = getLayoutInflater();
        for (String genre : Genres.ALL) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_genre, genresGroup, false);
            chip.setText(genre);
            chip.setCheckable(true);
            chip.setTag(genre);
            chip.setOnCheckedChangeListener((button, checked) -> {
                if (checked) {
                    selectedGenres.add(genre);
                } else {
                    selectedGenres.remove(genre);
                }
            });
            genresGroup.addView(chip);
        }
    }

    private void fillForm(MediaItem item) {
        titleField.setText(item.title);
        originalField.setText(item.originalTitle);
        yearField.setText(item.year == null ? null : String.valueOf(item.year));
        checkChipByTag(typeGroup, item.type);
        checkChipByTag(statusGroup, item.status);
        selectedGenres.clear();
        selectedGenres.addAll(item.genres);
        for (int i = 0; i < genresGroup.getChildCount(); i++) {
            View child = genresGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setChecked(item.genres.contains(chip.getTag()));
            }
        }
        directorsField.setText(joinComma(item.directors));
        castField.setText(joinComma(item.cast));
        tagsField.setText(joinComma(item.tags));
        overviewField.setText(item.overview);
        runtimeField.setText(item.runtimeMinutes == null
                ? null : String.valueOf(item.runtimeMinutes));
        episodesField.setText(item.totalEpisodes == null
                ? null : String.valueOf(item.totalEpisodes));
        ratingBar.setRating(item.rating == null ? 0f : item.rating / 2f);
        updateRatingLabel();
        updateEpisodesVisibility();
    }

    private void save() {
        String title = titleField.getText().toString().trim();
        if (title.isEmpty()) {
            titleLayout.setError(getString(R.string.title_required));
            return;
        }
        titleLayout.setError(null);

        invalidNumber = false;
        Integer year = numberOrNull(yearField, yearLayout);
        Integer runtime = numberOrNull(runtimeField, runtimeLayout);
        Integer episodes = numberOrNull(episodesField, episodesLayout);
        if (invalidNumber) {
            return;
        }

        MediaItem item = new MediaItem();
        if (loaded != null) {
            item.id = loaded.id;
            item.createdAt = loaded.createdAt;
            item.source = loaded.source;
            item.poster = loaded.poster;
            item.tmdbId = loaded.tmdbId;
            item.imdbId = loaded.imdbId;
        }
        item.title = title;
        item.originalTitle = textOrNull(originalField);
        item.year = year;
        item.type = checkedTag(typeGroup, MediaType.MOVIE);
        item.status = checkedTag(statusGroup, MediaStatus.WANT);
        List<String> genres = new ArrayList<>();
        for (String genre : Genres.ALL) {
            if (selectedGenres.contains(genre)) {
                genres.add(genre);
            }
        }
        item.genres = genres;
        item.directors = splitComma(directorsField);
        item.cast = splitComma(castField);
        item.tags = splitComma(tagsField);
        item.overview = textOrNull(overviewField);
        item.runtimeMinutes = runtime;
        item.totalEpisodes = MediaType.SERIES.equals(item.type) ? episodes : null;
        item.rating = ratingBar.getRating() <= 0f
                ? null : Math.min(ratingBar.getRating() * 2f, 10f);

        vm.save(item, savedId -> finish());
    }

    private void updateEpisodesVisibility() {
        boolean series = MediaType.SERIES.equals(checkedTag(typeGroup, MediaType.MOVIE));
        episodesLayout.setVisibility(series ? View.VISIBLE : View.GONE);
    }

    private void updateRatingLabel() {
        float rating = ratingBar.getRating();
        if (rating <= 0f) {
            ratingValue.setText(R.string.not_rated);
            ratingBar.setContentDescription(null);
        } else {
            String score = MediaMeta.score(rating * 2f);
            ratingValue.setText(getString(R.string.rating_value, score));
            ratingBar.setContentDescription(
                    getString(R.string.rating_description, score));
        }
    }

    @Nullable
    private Integer numberOrNull(TextInputEditText field, TextInputLayout layout) {
        String text = field.getText().toString().trim();
        if (text.isEmpty()) {
            layout.setError(null);
            return null;
        }
        try {
            int value = Integer.parseInt(text);
            layout.setError(null);
            return value;
        } catch (NumberFormatException e) {
            layout.setError(getString(R.string.invalid_number));
            invalidNumber = true;
            return null;
        }
    }

    @Nullable
    private static String textOrNull(TextInputEditText field) {
        String text = field.getText().toString().trim();
        return text.isEmpty() ? null : text;
    }

    private static List<String> splitComma(TextInputEditText field) {
        List<String> parts = new ArrayList<>();
        String text = field.getText().toString();
        for (String part : text.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }
        return parts;
    }

    private static String joinComma(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(value);
        }
        return sb.toString();
    }

    private static String checkedTag(ChipGroup group, String fallback) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Chip && ((Chip) child).isChecked()) {
                Object tag = child.getTag();
                if (tag instanceof String) {
                    return (String) tag;
                }
            }
        }
        return fallback;
    }

    private static void checkChipByTag(ChipGroup group, String tag) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(tag != null && tag.equals(child.getTag()));
            }
        }
    }
}
