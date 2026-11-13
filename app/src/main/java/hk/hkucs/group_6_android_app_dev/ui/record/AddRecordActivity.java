package hk.hkucs.group_6_android_app_dev.ui.record;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.SingleLineTransformationMethod;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Pattern;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;

public class AddRecordActivity extends AppCompatActivity {
    public static final String EXTRA_MEDIA_ID =
            "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

    private static final String DATE_PICKER_TAG = "watch_date_picker";
    private static final int MIN_WATCH_YEAR = 1900;
    private static final int MAX_WATCH_YEAR = 2100;

    private static final Pattern RATING_INPUT =
            Pattern.compile("(?:[0-9](?:\\.[0-9]?)?|10(?:\\.0?)?)?");

    private MediaItem media;

    public static Intent newIntent(Context context, @NonNull String mediaId) {
        if (mediaId == null || mediaId.trim().isEmpty()) {
            throw new IllegalArgumentException("mediaId is required");
        }
        return new Intent(context, AddRecordActivity.class)
                .putExtra(EXTRA_MEDIA_ID, mediaId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String mediaId = readMediaId();
        if (mediaId == null || mediaId.trim().isEmpty()) {
            Toast.makeText(this, R.string.invalid_navigation_arguments,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_record);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.record_root),
                (view, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                                    | WindowInsetsCompat.Type.ime());
                    view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
                    return windowInsets;
                });

        findViewById(R.id.record_toolbar).setOnClickListener(view -> finish());

        EditText dateInput = findViewById(R.id.record_date);
        EditText minutesInput = findViewById(R.id.record_minutes);
        EditText ratingInput = findViewById(R.id.record_rating);
        ratingInput.setFilters(new InputFilter[] {AddRecordActivity::filterRating});
        findViewById(R.id.record_clear_rating).setOnClickListener(view ->
                ratingInput.getText().clear());
        EditText noteInput = findViewById(R.id.record_note);
        TextView reviewCount = findViewById(R.id.record_review_count);
        updateReviewCount(reviewCount, noteInput.getText());
        noteInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                updateReviewCount(reviewCount, text);
            }

            @Override
            public void afterTextChanged(Editable text) {
            }
        });
        Button saveButton = findViewById(R.id.record_save);

        findViewById(R.id.record_clear_review).setOnClickListener(view ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.record_clear_review_confirmation)
                        .setNegativeButton(R.string.record_dialog_cancel, null)
                        .setPositiveButton(R.string.record_clear_review,
                                (dialog, which) -> noteInput.getText().clear())
                        .show());

        noteInput.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                view.getParent().requestDisallowInterceptTouchEvent(
                        view.canScrollVertically(-1) || view.canScrollVertically(1));
            }
            return false;
        });

        Calendar today = Calendar.getInstance();
        dateInput.setText(formatDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)));
        MaterialDatePicker<?> restoredPicker = (MaterialDatePicker<?>)
                getSupportFragmentManager().findFragmentByTag(DATE_PICKER_TAG);
        if (restoredPicker != null) {
            bindDatePicker(restoredPicker, dateInput);
        }
        findViewById(R.id.record_select_date).setOnClickListener(view -> showDatePicker(dateInput));

        AddRecordViewModel viewModel =
                new ViewModelProvider(this).get(AddRecordViewModel.class);

        View episodesGroup = findViewById(R.id.record_episodes_group);
        EditText episodesInput = findViewById(R.id.record_episodes);
        Button selectEpisodes = findViewById(R.id.record_select_episodes);
        saveButton.setEnabled(false);
        viewModel.observeMedia(mediaId).observe(this, item -> {
            media = item;
            boolean series = isSeries();
            episodesGroup.setVisibility(series ? View.VISIBLE : View.GONE);
            selectEpisodes.setEnabled(series && episodeLimit() > 0);
            saveButton.setEnabled(item != null
                    && viewModel.getSaveState().getValue() == AddRecordViewModel.SaveState.READY);
            if (item == null) {
                Toast.makeText(this, R.string.invalid_navigation_arguments, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        selectEpisodes.setOnClickListener(view -> showEpisodePicker(episodesInput));
        episodesInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return false;
            }
            TreeSet<Integer> selected = readEpisodes(episodesInput);
            if (selected == null) {
                return true;
            }
            episodesInput.setText(TextUtils.join(",", selected));
            return false;
        });

        viewModel.getSaveState().observe(this, state -> {
            saveButton.setEnabled(media != null && state == AddRecordViewModel.SaveState.READY);
            if (state == AddRecordViewModel.SaveState.SAVED) {
                finish();
            }
        });

        saveButton.setOnClickListener(view -> {
            String date = dateInput.getText().toString().trim();
            if (parseWatchDate(date) == null) {
                dateInput.setError(getString(R.string.record_date_error));
                dateInput.requestFocus();
                return;
            }
            dateInput.setError(null);

            String minutesText = minutesInput.getText().toString().trim();
            Integer minutes = null;
            if (!minutesText.isEmpty()) {
                try {
                    minutes = Integer.valueOf(minutesText);
                } catch (NumberFormatException exception) {
                    showMinutesError(minutesInput);
                    return;
                }
                if (minutes <= 0) {
                    showMinutesError(minutesInput);
                    return;
                }
            }

            List<Integer> episodes = Collections.emptyList();
            if (isSeries()) {
                TreeSet<Integer> selected = readEpisodes(episodesInput);
                if (selected == null) {
                    return;
                }
                episodes = new ArrayList<>(selected);
                episodesInput.setText(TextUtils.join(",", selected));
            }
            String ratingText = ratingInput.getText().toString();
            Float rating = ratingText.isEmpty() ? null : Float.valueOf(ratingText);
            viewModel.save(
                    mediaId,
                    date,
                    minutes,
                    rating,
                    noteInput.getText().toString().trim(), episodes);
        });
    }

    private static CharSequence filterRating(CharSequence source, int start, int end,
                                             Spanned destination,
                                             int replaceStart, int replaceEnd) {
        String prefix = destination.subSequence(0, replaceStart).toString();
        String suffix = destination.subSequence(replaceEnd, destination.length()).toString();
        String candidate = prefix + source.subSequence(start, end) + suffix;
        if (RATING_INPUT.matcher(candidate).matches()) {
            return null;
        }

        int decimalPoint = candidate.indexOf('.');
        int keepThrough = decimalPoint + 2;
        int insertionEnd = candidate.length() - suffix.length();
        if (decimalPoint >= 0 && keepThrough >= replaceStart && keepThrough < insertionEnd
                && candidate.substring(keepThrough, insertionEnd).matches("[0-9]+")
                && RATING_INPUT.matcher(candidate.substring(0, keepThrough) + suffix).matches()) {
            return source.subSequence(start, start + keepThrough - replaceStart);
        }
        return destination.subSequence(replaceStart, replaceEnd);
    }

    private void updateReviewCount(TextView reviewCount, CharSequence review) {
        int count = Character.codePointCount(review, 0, review.length());
        reviewCount.setText(getResources().getQuantityString(
                R.plurals.record_review_character_count, count, count));
    }

    private boolean isSeries() {
        return media != null && MediaType.SERIES.equals(media.type);
    }

    private int episodeLimit() {
        return media != null && media.totalEpisodes != null ? media.totalEpisodes : 0;
    }

    private TreeSet<Integer> readEpisodes(EditText input) {
        TreeSet<Integer> selected = new TreeSet<>();
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            input.setError(null);
            return selected;
        }

        int limit = episodeLimit();
        try {
            for (String token : text.split(",", -1)) {
                int episode = Integer.parseInt(token.trim());
                if (episode < 1 || (limit > 0 && episode > limit)) {
                    throw new NumberFormatException();
                }
                selected.add(episode);
            }
            input.setError(null);
            return selected;
        } catch (NumberFormatException exception) {
            input.setError(limit > 0 ? getString(R.string.record_episodes_range, limit)
                    : getString(R.string.record_episodes_positive));
            input.requestFocus();
            return null;
        }
    }

    private void showEpisodePicker(EditText input) {
        int limit = episodeLimit();
        if (limit <= 0) {
            return;
        }
        TreeSet<Integer> selected = readEpisodes(input);
        if (selected == null) {
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        GridView grid = new GridView(builder.getContext());
        int spacing = getResources().getDimensionPixelSize(R.dimen.space_12);
        int leftPadding = getResources().getDimensionPixelSize(R.dimen.space_24);
        int rightPadding = getResources().getDimensionPixelSize(R.dimen.space_8);
        int verticalPadding = getResources().getDimensionPixelSize(R.dimen.space_16);
        int tileWidth = getResources().getDimensionPixelSize(R.dimen.touch_target);
        grid.setNumColumns(GridView.AUTO_FIT);
        grid.setColumnWidth(tileWidth);
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setGravity(Gravity.CENTER);
        grid.setHorizontalSpacing(spacing);
        grid.setVerticalSpacing(spacing);
        grid.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
        grid.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        grid.setPadding(leftPadding, verticalPadding, rightPadding, verticalPadding);
        grid.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return limit;
            }

            @Override
            public Integer getItem(int position) {
                return position + 1;
            }

            @Override
            public long getItemId(int position) {
                return getItem(position);
            }

            @Override
            public View getView(int position, View recycled, ViewGroup parent) {
                MaterialButton tile = recycled instanceof MaterialButton
                        ? (MaterialButton) recycled : new MaterialButton(grid.getContext());
                int episode = getItem(position);
                tile.setLayoutParams(new AbsListView.LayoutParams(tileWidth, tileWidth));
                tile.setMinimumWidth(0);
                tile.setInsetTop(0);
                tile.setInsetBottom(0);
                tile.setPadding(0, 0, 0, 0);
                tile.setText(Integer.toString(episode));
                tile.setCheckable(true);
                tile.setChecked(selected.contains(episode));
                tile.setClickable(false);
                tile.setFocusable(false);
                tile.setBackgroundTintList(episodeTileColors(R.color.primary, R.color.surface_variant));
                tile.setTextColor(episodeTileColors(R.color.on_primary, R.color.text_primary));
                return tile;
            }
        });
        grid.setOnItemClickListener((parent, view, position, id) -> {
            int episode = position + 1;
            boolean checked = selected.add(episode);
            if (!checked) {
                selected.remove(episode);
            }
            ((MaterialButton) view).setChecked(checked);
        });

        builder.setTitle(R.string.record_watched_episodes)
                .setView(grid)
                .setNegativeButton(R.string.record_dialog_cancel, null)
                .setPositiveButton(R.string.record_dialog_ok, (dialog, which) -> {
                    input.setText(TextUtils.join(",", selected));
                    input.setError(null);
                })
                .show();
    }

    private ColorStateList episodeTileColors(int checked, int unchecked) {
        return new ColorStateList(new int[][] {{android.R.attr.state_checked}, {}},
                new int[] {ContextCompat.getColor(this, checked), ContextCompat.getColor(this, unchecked)});
    }

    private String readMediaId() {
        try {
            return getIntent().getStringExtra(EXTRA_MEDIA_ID);
        } catch (RuntimeException invalidExtras) {
            return null;
        }
    }

    private void showMinutesError(EditText minutesInput) {
        minutesInput.setError(getString(R.string.record_minutes_error));
        minutesInput.requestFocus();
    }

    private void showDatePicker(EditText dateInput) {
        if (getSupportFragmentManager().findFragmentByTag(DATE_PICKER_TAG) != null) {
            return;
        }
        Long selection = parseWatchDate(dateInput.getText().toString().trim());
        Calendar calendar = utcCalendar();
        calendar.set(MIN_WATCH_YEAR, Calendar.JANUARY, 1);
        long start = calendar.getTimeInMillis();
        calendar.set(MAX_WATCH_YEAR, Calendar.DECEMBER, 31);
        long end = calendar.getTimeInMillis();
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(start)
                .setEnd(end)
                .setOpenAt(selection != null ? selection
                        : Math.max(start, Math.min(end, MaterialDatePicker.todayInUtcMilliseconds())))
                .build();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.record_watch_date)
                .setNegativeButtonText(R.string.record_dialog_cancel)
                .setPositiveButtonText(R.string.record_dialog_ok)
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setCalendarConstraints(constraints)
                .setSelection(selection)
                .build();
        bindDatePicker(picker, dateInput);
        picker.showNow(getSupportFragmentManager(), DATE_PICKER_TAG);
    }

    @SuppressWarnings("PrivateResource")
    private void bindDatePicker(MaterialDatePicker<?> picker, EditText dateInput) {
        picker.addOnPositiveButtonClickListener(selection -> {
            dateInput.setText(formatWatchDate((Long) selection));
            dateInput.setError(null);
        });
        picker.getViewLifecycleOwnerLiveData().observe(picker, owner -> {
            if (owner == null) {
                return;
            }
            View pickerView = picker.requireView();
            // The calendar's child view is created when the dialog starts.
            pickerView.post(() -> setEnglishMonthTitle(pickerView));
            View inputModeToggle = pickerView.findViewById(
                    com.google.android.material.R.id.mtrl_picker_header_toggle);
            if (inputModeToggle != null) {
                inputModeToggle.setVisibility(View.GONE);
            }
            GridView weekdays = pickerView.findViewById(
                    com.google.android.material.R.id.mtrl_calendar_days_of_week);
            if (weekdays != null) {
                setShortWeekdayLabels(weekdays);
            }
            TextView header = pickerView.findViewById(
                    com.google.android.material.R.id.mtrl_picker_header_selection_text);
            if (header != null) {
                header.setTransformationMethod(new SingleLineTransformationMethod() {
                    @Override
                    public CharSequence getTransformation(CharSequence source, View view) {
                        Long selection = (Long) picker.getSelection();
                        return selection == null ? source : formatWatchDate(selection);
                    }
                });
            }
        });
    }

    @SuppressWarnings("PrivateResource")
    private static void setEnglishMonthTitle(View pickerView) {
        TextView monthTitle = pickerView.findViewById(
                com.google.android.material.R.id.month_navigation_fragment_toggle);
        if (monthTitle == null) {
            return;
        }

        DateFormat localFormat = DateFormat.getInstanceForSkeleton(
                DateFormat.YEAR_MONTH, Locale.getDefault());
        DateFormat englishFormat = DateFormat.getInstanceForSkeleton(
                DateFormat.YEAR_MONTH, Locale.US);
        localFormat.setTimeZone(android.icu.util.TimeZone.getTimeZone("UTC"));
        englishFormat.setTimeZone(android.icu.util.TimeZone.getTimeZone("UTC"));
        monthTitle.setTransformationMethod(new SingleLineTransformationMethod() {
            @Override
            public CharSequence getTransformation(CharSequence source, View view) {
                if (TextUtils.isEmpty(source)) {
                    return source;
                }
                ParsePosition position = new ParsePosition(0);
                Date month = localFormat.parse(source.toString(), position);
                return month != null && position.getIndex() == source.length()
                        ? englishFormat.format(month) : source;
            }
        });
    }

    private static void setShortWeekdayLabels(GridView weekdays) {
        ListAdapter original = weekdays.getAdapter();
        String[] labels = DateFormatSymbols.getInstance(Locale.US).getShortWeekdays();
        weekdays.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return original.getCount();
            }

            @Override
            public Integer getItem(int position) {
                return (Integer) original.getItem(position);
            }

            @Override
            public long getItemId(int position) {
                return original.getItemId(position);
            }

            @Override
            public View getView(int position, View recycled, ViewGroup parent) {
                TextView label = (TextView) original.getView(position, recycled, parent);
                label.setText(labels[getItem(position)]);
                return label;
            }
        });
    }

    private static String formatWatchDate(long dateMillis) {
        Calendar calendar = utcCalendar();
        calendar.setTimeInMillis(dateMillis);
        return formatDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    private static Long parseWatchDate(String date) {
        if (!date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            return null;
        }

        int year = Integer.parseInt(date.substring(0, 4));
        if (year < MIN_WATCH_YEAR || year > MAX_WATCH_YEAR) {
            return null;
        }

        Calendar calendar = utcCalendar();
        calendar.setLenient(false);
        calendar.set(year, Integer.parseInt(date.substring(5, 7)) - 1,
                Integer.parseInt(date.substring(8, 10)));
        try {
            return calendar.getTimeInMillis();
        } catch (IllegalArgumentException invalidDate) {
            return null;
        }
    }

    private static Calendar utcCalendar() {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        calendar.clear();
        return calendar;
    }

    private static String formatDate(int year, int zeroBasedMonth, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, zeroBasedMonth + 1, day);
    }
}
