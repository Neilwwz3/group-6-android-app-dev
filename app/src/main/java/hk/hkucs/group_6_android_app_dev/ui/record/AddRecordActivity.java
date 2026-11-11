//package hk.hkucs.group_6_android_app_dev.ui.record;
//
//import android.content.Context;
//import android.content.Intent;
//import androidx.annotation.NonNull;
//import hk.hkucs.group_6_android_app_dev.R;
//import hk.hkucs.group_6_android_app_dev.ui.components.FeaturePlaceholderActivity;
//
///** G1 entry contract for the future G3 implementation. */
//public class AddRecordActivity extends FeaturePlaceholderActivity {
//    public static final String EXTRA_MEDIA_ID = "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";
//
//    public static Intent newIntent(Context context, @NonNull String mediaId) {
//        if (mediaId == null || mediaId.trim().isEmpty()) { throw new IllegalArgumentException("mediaId is required"); }
//        return new Intent(context, AddRecordActivity.class).putExtra(EXTRA_MEDIA_ID, mediaId);
//    }
//    @Override protected boolean hasValidArguments() { return hasValidMediaId(EXTRA_MEDIA_ID, true); }
//    @Override protected int titleResource() { return R.string.record_title; }
//    @Override protected int messageResource() { return R.string.record_placeholder; }
//}
package hk.hkucs.group_6_android_app_dev.ui.record;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;
import java.util.Locale;

import hk.hkucs.group_6_android_app_dev.R;

public class AddRecordActivity extends AppCompatActivity {
    public static final String EXTRA_MEDIA_ID =
            "hk.hkucs.group_6_android_app_dev.extra.MEDIA_ID";

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

        Button dateButton = findViewById(R.id.record_date);
        EditText minutesInput = findViewById(R.id.record_minutes);
        RatingBar ratingInput = findViewById(R.id.record_rating);
        EditText noteInput = findViewById(R.id.record_note);
        Button saveButton = findViewById(R.id.record_save);

        noteInput.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                view.getParent().requestDisallowInterceptTouchEvent(
                        view.canScrollVertically(-1) || view.canScrollVertically(1));
            }
            return false;
        });

        Calendar today = Calendar.getInstance();
        dateButton.setText(formatDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)));
        dateButton.setOnClickListener(view -> showDatePicker(dateButton));

        AddRecordViewModel viewModel =
                new ViewModelProvider(this).get(AddRecordViewModel.class);

        viewModel.getSaveState().observe(this, state -> {
            saveButton.setEnabled(state == AddRecordViewModel.SaveState.READY);
            if (state == AddRecordViewModel.SaveState.SAVED) {
                finish();
            }
        });

        saveButton.setOnClickListener(view -> {
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

            viewModel.save(
                    mediaId,
                    dateButton.getText().toString(),
                    minutes,
                    ratingInput.getRating() * 2f,
                    noteInput.getText().toString().trim());
        });
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

    private void showDatePicker(Button dateButton) {
        String[] date = dateButton.getText().toString().split("-");
        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]);
        int day = Integer.parseInt(date[2]);

        Context dialogContext = new ContextThemeWrapper(
                this, android.R.style.Theme_Holo_Light_Dialog);
        LinearLayout pickers = new LinearLayout(dialogContext);
        pickers.setLayoutDirection(LinearLayout.LAYOUT_DIRECTION_LTR);
        int padding = getResources().getDimensionPixelSize(R.dimen.space_16);
        pickers.setPadding(padding, 0, padding, 0);

        NumberPicker yearPicker = addDateSpinner(pickers, 1900, 2100, year, "%04d");
        NumberPicker monthPicker = addDateSpinner(pickers, 1, 12, month, "%02d");
        NumberPicker dayPicker = addDateSpinner(
                pickers, 1, daysInMonth(year, month), day, "%02d");
        NumberPicker.OnValueChangeListener updateDayRange = (picker, oldValue, newValue) ->
                dayPicker.setMaxValue(daysInMonth(yearPicker.getValue(), monthPicker.getValue()));
        yearPicker.setOnValueChangedListener(updateDayRange);
        monthPicker.setOnValueChangedListener(updateDayRange);

        new AlertDialog.Builder(dialogContext, android.R.style.Theme_Holo_Light_Dialog)
                .setTitle(R.string.record_watch_date)
                .setView(pickers)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    yearPicker.clearFocus();
                    monthPicker.clearFocus();
                    dayPicker.clearFocus();
                    int selectedYear = yearPicker.getValue();
                    int selectedMonth = monthPicker.getValue();
                    int selectedDay = Math.min(
                            dayPicker.getValue(), daysInMonth(selectedYear, selectedMonth));
                    dateButton.setText(formatDate(selectedYear, selectedMonth - 1, selectedDay));
                })
                .show();
    }

    private static NumberPicker addDateSpinner(
            LinearLayout row, int min, int max, int value, String format) {
        NumberPicker picker = new NumberPicker(row.getContext());
        picker.setMinValue(min);
        picker.setMaxValue(max);
        picker.setFormatter(number -> String.format(Locale.US, format, number));
        picker.setValue(value);
        row.addView(picker, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return picker;
    }

    private static int daysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.clear();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private static String formatDate(int year, int zeroBasedMonth, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, zeroBasedMonth + 1, day);
    }
}
