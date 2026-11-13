package hk.hkucs.group_6_android_app_dev.ui.profile;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;
import hk.hkucs.group_6_android_app_dev.ui.components.DonutChartView;

public class ProfileFragment extends Fragment {
    private static final int GENRE_PREVIEW_SIZE = 3;
    private AlertDialog dialog;
    private boolean genresExpanded;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        ProfileViewModel model = new ViewModelProvider(this).get(ProfileViewModel.class);
        View load = view.findViewById(R.id.profile_load);
        View clear = view.findViewById(R.id.profile_clear);

        model.getSummary().observe(getViewLifecycleOwner(), summary -> renderSummary(view, summary));
        model.getLoggedTitleCounts().observe(getViewLifecycleOwner(), counts -> {
            NumberFormat number = NumberFormat.getIntegerInstance(
                    getResources().getConfiguration().getLocales().get(0));
            setText(view, R.id.profile_logged_movies, number.format(counts.movies));
            setText(view, R.id.profile_logged_series, number.format(counts.series));
        });
        model.getGenreCounts().observe(getViewLifecycleOwner(), counts -> renderGenres(view, counts));
        model.getBusy().observe(getViewLifecycleOwner(), busy -> {
            boolean running = Boolean.TRUE.equals(busy);
            load.setEnabled(!running);
            clear.setEnabled(!running);
            view.findViewById(R.id.profile_progress).setVisibility(running ? View.VISIBLE : View.GONE);
        });
        model.getLoadFailed().observe(getViewLifecycleOwner(), failed -> {
            if (Boolean.TRUE.equals(failed)) {
                Toast.makeText(requireContext(), R.string.g5_profile_load_error, Toast.LENGTH_LONG).show();
                model.acknowledgeLoadFailure();
            }
        });

        load.setOnClickListener(button -> confirm(R.string.g5_profile_load, R.string.g5_profile_load_message,
                model::loadSampleData));
        clear.setOnClickListener(button -> confirm(R.string.g5_profile_clear, R.string.g5_profile_clear_message,
                model::clearAllData));
        view.findViewById(R.id.profile_about).setOnClickListener(button ->
                dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.g5_profile_about_message)
                        .setPositiveButton(R.string.g5_profile_ok, null)
                        .show());
    }

    private void renderSummary(View view, StatsSummary summary) {
        if (summary == null) {
            return;
        }
        view.findViewById(R.id.profile_empty).setVisibility(summary.mediaCount == 0 ? View.VISIBLE : View.GONE);
        NumberFormat integerFormat = NumberFormat.getIntegerInstance(getResources().getConfiguration().getLocales().get(0));
        setText(view, R.id.profile_titles, integerFormat.format(summary.mediaCount));
        setText(view, R.id.profile_watched, integerFormat.format(summary.watchedCount));
        setText(view, R.id.profile_time,
                getString(R.string.g5_profile_metric_minutes, integerFormat.format(summary.totalMinutes)));
        CharSequence rating = getText(R.string.g5_profile_metric_unrated);
        if (summary.averageRating != null) {
            SpannableStringBuilder formatted = new SpannableStringBuilder(
                    getString(R.string.g5_profile_metric_rating_value, summary.averageRating));
            int suffixStart = formatted.length();
            formatted.append(getText(R.string.g5_profile_metric_rating_scale));
            formatted.setSpan(new TextAppearanceSpan(view.getContext(), R.style.TextAppearance_Group6_Caption),
                    suffixStart, formatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            rating = formatted;
        }
        setText(view, R.id.profile_average, rating);
    }

    private void renderGenres(View view, List<GenreCount> counts) {
        DonutChartView chart = view.findViewById(R.id.profile_chart);
        LinearLayout legend = view.findViewById(R.id.profile_legend);
        boolean hasGenres = counts != null && !counts.isEmpty();
        view.findViewById(R.id.profile_chart_container).setVisibility(hasGenres ? View.VISIBLE : View.GONE);
        legend.setVisibility(hasGenres ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.profile_no_genres).setVisibility(hasGenres ? View.GONE : View.VISIBLE);
        View toggle = view.findViewById(R.id.profile_toggle_genres);
        if (!hasGenres) {
            genresExpanded = false;
            legend.removeAllViews();
            toggle.setVisibility(View.GONE);
            toggle.setOnClickListener(null);
            view.findViewById(R.id.profile_genre_share_note).setVisibility(View.GONE);
            return;
        }

        chart.setCounts(counts);
        int genreTotal = counts.size();
        NumberFormat number = NumberFormat.getIntegerInstance(getResources().getConfiguration().getLocales().get(0));
        setText(view, R.id.profile_genre_total, number.format(genreTotal));
        setText(view, R.id.profile_genre_total_label,
                getResources().getQuantityText(R.plurals.g5_profile_genre_total_label, genreTotal));
        ViewCompat.setScreenReaderFocusable(view.findViewById(R.id.profile_genre_center), true);
        toggle.setOnClickListener(button -> {
            genresExpanded = !genresExpanded;
            renderGenreLegend(view, legend, chart, counts);
        });
        renderGenreLegend(view, legend, chart, counts);
    }

    private void renderGenreLegend(View view, LinearLayout legend, DonutChartView chart, List<GenreCount> counts) {
        boolean canExpand = counts.size() > GENRE_PREVIEW_SIZE;
        boolean expanded = canExpand && genresExpanded;
        int visibleCount = expanded ? counts.size() : Math.min(GENRE_PREVIEW_SIZE, counts.size());
        legend.removeAllViews();
        for (int i = 0; i < visibleCount; i++) {
            addGenreRow(legend, counts.get(i), chart.getSegmentColor(i), chart.getPercentage(i));
        }
        view.findViewById(R.id.profile_toggle_genres).setVisibility(canExpand ? View.VISIBLE : View.GONE);
        setText(view, R.id.profile_toggle_genres,
                getText(expanded ? R.string.g5_profile_show_fewer_genres : R.string.g5_profile_show_all_genres));
        view.findViewById(R.id.profile_genre_share_note).setVisibility(expanded ? View.VISIBLE : View.GONE);
    }

    private void addGenreRow(LinearLayout legend, GenreCount count, int color, String percentage) {
        int dotSize = getResources().getDimensionPixelSize(R.dimen.space_8);
        int spacing = getResources().getDimensionPixelSize(R.dimen.space_12);
        int verticalPadding = getResources().getDimensionPixelSize(R.dimen.space_4);

        LinearLayout row = new LinearLayout(legend.getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, verticalPadding, 0, verticalPadding);
        ViewCompat.setScreenReaderFocusable(row, true);

        View dot = new View(row.getContext());
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(color);
        dot.setBackground(circle);
        dot.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
        dotParams.setMarginEnd(spacing);
        row.addView(dot, dotParams);

        TextView name = new TextView(row.getContext());
        TextViewCompat.setTextAppearance(name, R.style.TextAppearance_Group6_Body);
        name.setText(count.genre);
        row.addView(name, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView quantity = new TextView(row.getContext());
        TextViewCompat.setTextAppearance(quantity, R.style.TextAppearance_Group6_Body);
        quantity.setText(NumberFormat.getIntegerInstance().format(count.count));
        LinearLayout.LayoutParams quantityParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        quantityParams.setMarginStart(spacing);
        row.addView(quantity, quantityParams);

        TextView share = new TextView(row.getContext());
        TextViewCompat.setTextAppearance(share, R.style.TextAppearance_Group6_Caption);
        share.setGravity(Gravity.END);
        share.setMinEms(4);
        share.setText(percentage);
        LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        shareParams.setMarginStart(spacing);
        row.addView(share, shareParams);
        legend.addView(row, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private static void setText(View view, int id, CharSequence text) {
        TextView label = view.findViewById(id);
        label.setText(text);
    }

    private void confirm(@StringRes int action, @StringRes int message, Runnable onConfirm) {
        dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(action)
                .setMessage(message)
                .setNegativeButton(R.string.g5_profile_cancel, null)
                .setPositiveButton(action, (window, which) -> onConfirm.run())
                .show();
    }

    @Override
    public void onDestroyView() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroyView();
    }
}
