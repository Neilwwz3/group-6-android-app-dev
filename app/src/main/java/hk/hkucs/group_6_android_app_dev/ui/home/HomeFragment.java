package hk.hkucs.group_6_android_app_dev.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import hk.hkucs.group_6_android_app_dev.MainActivity;
import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;
import hk.hkucs.group_6_android_app_dev.ui.detail.MediaDetailActivity;
import hk.hkucs.group_6_android_app_dev.util.PosterLoader;

public class HomeFragment extends Fragment {
    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);

        MainActivity activity = (MainActivity) requireActivity();
        view.findViewById(R.id.explore_library)
                .setOnClickListener(v -> activity.selectTab(R.id.nav_library));

        RecyclerView watchingList = view.findViewById(R.id.home_watching_list);
        WatchingAdapter watchingAdapter = new WatchingAdapter(this::openDetails);
        watchingList.setLayoutManager(new LinearLayoutManager(requireContext(),
                RecyclerView.HORIZONTAL, false));
        watchingList.setAdapter(watchingAdapter);

        HomeViewModel model = new ViewModelProvider(this).get(HomeViewModel.class);
        model.watching.observe(getViewLifecycleOwner(), items -> {
            watchingAdapter.submitList(items);
            watchingList.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
            view.findViewById(R.id.home_watching_empty)
                    .setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });
        model.recent.observe(getViewLifecycleOwner(), entries -> {
            showRecent(view, entries);
        });
        TextView watchCount = view.findViewById(R.id.home_month_count);
        TextView watchHours = view.findViewById(R.id.home_month_hours);
        MaterialButton monthSelector = view.findViewById(R.id.home_month_selector);
        View previousMonth = view.findViewById(R.id.home_month_previous);
        View nextMonth = view.findViewById(R.id.home_month_next);
        previousMonth.setOnClickListener(v -> model.moveMonth(-1));
        nextMonth.setOnClickListener(v -> model.moveMonth(1));
        monthSelector.setOnClickListener(v -> showMonthPicker(model));
        model.selectedMonth.observe(getViewLifecycleOwner(), value -> {
            monthSelector.setText(formatMonthLabel(value));
            nextMonth.setEnabled(model.canMoveNext());
        });
        model.monthSummary.observe(getViewLifecycleOwner(), summary -> {
            watchCount.setText(getString(R.string.home_month_count, summary.recordCount));
            watchHours.setText(formatDuration(summary.totalMinutes));
        });
        model.summary.observe(getViewLifecycleOwner(), summary -> updateContent(view, summary));
    }

    private void updateContent(View view, StatsSummary summary) {
        if (summary == null) {
            return;
        }
        boolean hasNoData = shouldShowWelcome(summary);
        view.findViewById(R.id.home_welcome)
                .setVisibility(hasNoData ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.home_dashboard)
                .setVisibility(hasNoData ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.home_subtitle)
                .setVisibility(hasNoData ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.home_eyebrow)
                .setVisibility(View.VISIBLE);
    }

    private void showRecent(View view, List<RecordWithMedia> entries) {
        LinearLayout rows = view.findViewById(R.id.home_recent_list);
        rows.removeAllViews();
        view.findViewById(R.id.home_recent_empty)
                .setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = getLayoutInflater();
        for (RecordWithMedia entry : entries) {
            View row = inflater.inflate(R.layout.item_home_record, rows, false);
            TextView title = row.findViewById(R.id.home_record_title);
            title.setText(entry.mediaTitle);
            ImageView poster = row.findViewById(R.id.home_record_poster);
            View placeholder = row.findViewById(R.id.home_record_placeholder);
            PosterLoader.load(requireContext(), entry.mediaPoster, poster, placeholder);
            TextView metadata = row.findViewById(R.id.home_record_metadata);
            String date = formatRecordDate(entry.record.date);
            String type = getString(MediaType.SERIES.equals(entry.mediaType)
                    ? R.string.home_type_series : R.string.home_type_movie);
            String details = entry.record.minutes == null
                    ? getString(R.string.home_record_metadata_no_duration, date, type)
                    : getString(R.string.home_record_metadata, date, type,
                            entry.record.minutes);
            metadata.setText(details);
            row.setContentDescription(getString(R.string.home_open_record,
                    entry.mediaTitle, details));
            row.setOnClickListener(v -> openDetails(entry.record.mediaId));
            rows.addView(row);
        }
    }

    private String formatDuration(int totalMinutes) {
        int safeMinutes = Math.max(0, totalMinutes);
        int hours = safeMinutes / 60;
        int minutes = safeMinutes % 60;
        if (hours == 0) {
            return getString(R.string.home_month_minutes, minutes);
        }
        if (minutes == 0) {
            return getString(R.string.home_month_whole_hours, hours);
        }
        return getString(R.string.home_month_hours_minutes, hours, minutes);
    }

    static boolean shouldShowWelcome(StatsSummary summary) {
        return summary != null && summary.mediaCount == 0 && summary.recordCount == 0;
    }

    static String formatRecordDate(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        input.setLenient(false);
        input.setTimeZone(TimeZone.getTimeZone("UTC"));
        ParsePosition position = new ParsePosition(0);
        Date parsed = input.parse(value, position);
        if (parsed == null || position.getIndex() != value.length()) {
            return value;
        }
        SimpleDateFormat output = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        output.setTimeZone(TimeZone.getTimeZone("UTC"));
        return output.format(parsed);
    }

    static String formatMonthLabel(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM", Locale.US);
        input.setLenient(false);
        input.setTimeZone(TimeZone.getTimeZone("UTC"));
        ParsePosition position = new ParsePosition(0);
        Date parsed = input.parse(value, position);
        if (parsed == null || position.getIndex() != value.length()) {
            return value;
        }
        SimpleDateFormat output = new SimpleDateFormat("MMMM yyyy", Locale.US);
        output.setTimeZone(TimeZone.getTimeZone("UTC"));
        return output.format(parsed);
    }

    private void showMonthPicker(HomeViewModel model) {
        View pickerView = getLayoutInflater().inflate(R.layout.dialog_home_month_picker, null);
        NumberPicker yearPicker = pickerView.findViewById(R.id.home_month_picker_year);
        NumberPicker monthPicker = pickerView.findViewById(R.id.home_month_picker_month);
        Calendar now = Calendar.getInstance(Locale.US);
        int currentYear = now.get(Calendar.YEAR);
        int currentMonth = now.get(Calendar.MONTH) + 1;
        String[] selected = model.getSelectedMonth().split("-");
        int selectedYear = Integer.parseInt(selected[0]);
        int selectedMonth = Integer.parseInt(selected[1]);
        String[] months = Arrays.copyOf(new DateFormatSymbols(Locale.US).getMonths(), 12);

        yearPicker.setMinValue(1900);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setWrapSelectorWheel(false);
        yearPicker.setValue(Math.max(1900, Math.min(currentYear, selectedYear)));
        configureMonthPicker(monthPicker, months,
                yearPicker.getValue() == currentYear ? currentMonth : 12,
                selectedMonth);
        yearPicker.setOnValueChangedListener((picker, oldValue, newValue) ->
                configureMonthPicker(monthPicker, months,
                        newValue == currentYear ? currentMonth : 12,
                        monthPicker.getValue()));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.home_choose_month_title)
                .setView(pickerView)
                .setNegativeButton(R.string.home_month_cancel, null)
                .setPositiveButton(R.string.home_month_apply, (dialog, which) ->
                        model.selectMonth(yearPicker.getValue(), monthPicker.getValue()))
                .show();
    }

    private static void configureMonthPicker(NumberPicker picker, String[] months,
            int maxMonth, int selectedMonth) {
        picker.setDisplayedValues(null);
        picker.setMinValue(1);
        picker.setMaxValue(maxMonth);
        picker.setWrapSelectorWheel(false);
        picker.setDisplayedValues(Arrays.copyOf(months, maxMonth));
        picker.setValue(Math.max(1, Math.min(maxMonth, selectedMonth)));
    }

    private void openDetails(String mediaId) {
        startActivity(MediaDetailActivity.newIntent(requireContext(), mediaId));
    }
}
