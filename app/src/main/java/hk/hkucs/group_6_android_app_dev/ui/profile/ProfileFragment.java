package hk.hkucs.group_6_android_app_dev.ui.profile;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;
import hk.hkucs.group_6_android_app_dev.ui.components.DonutChartView;

public class ProfileFragment extends Fragment {
    private static final int GENRE_PREVIEW_SIZE = 3;
    private static final int MIN_SIDE_BY_SIDE_WIDTH_DP = 300;
    private static final float MAX_SIDE_BY_SIDE_FONT_SCALE = 1.1f;
    private AlertDialog dialog;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        ProfileViewModel model = new ViewModelProvider(this).get(ProfileViewModel.class);
        LinearLayout genreLayout = view.findViewById(R.id.profile_genre_layout);
        genreLayout.addOnLayoutChangeListener((layout, left, top, right, bottom,
                                               oldLeft, oldTop, oldRight, oldBottom) -> {
            if (right - left != oldRight - oldLeft) {
                updateGenreLayout(genreLayout);
            }
        });
        View load = view.findViewById(R.id.profile_load);
        View clear = view.findViewById(R.id.profile_clear);

        model.getSummary().observe(getViewLifecycleOwner(), summary -> renderSummary(view, summary));
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
        NumberFormat integerFormat = NumberFormat.getIntegerInstance(Locale.US);
        setText(view, R.id.profile_titles, integerFormat.format(summary.mediaCount));
        setText(view, R.id.profile_watched, integerFormat.format(summary.watchedCount));
        setText(view, R.id.profile_time,
                getString(R.string.g5_profile_metric_minutes, integerFormat.format(summary.totalMinutes)));
        CharSequence rating = getText(R.string.g5_profile_metric_unrated);
        if (summary.averageRating != null) {
            SpannableStringBuilder formatted = new SpannableStringBuilder(
                    String.format(Locale.US, getString(R.string.g5_profile_metric_rating_value),
                            summary.averageRating));
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
        TableLayout legend = view.findViewById(R.id.profile_legend);
        boolean hasGenres = counts != null && !counts.isEmpty();
        view.findViewById(R.id.profile_genre_layout).setVisibility(hasGenres ? View.VISIBLE : View.GONE);
        legend.setVisibility(hasGenres ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.profile_no_genres).setVisibility(hasGenres ? View.GONE : View.VISIBLE);
        View toggle = view.findViewById(R.id.profile_toggle_genres);
        if (!hasGenres) {
            legend.removeAllViews();
            toggle.setVisibility(View.GONE);
            toggle.setOnClickListener(null);
            return;
        }

        chart.setCounts(counts);
        int genreTotal = counts.size();
        NumberFormat number = NumberFormat.getIntegerInstance(Locale.US);
        setText(view, R.id.profile_genre_total, number.format(genreTotal));
        setText(view, R.id.profile_genre_total_label,
                getResources().getQuantityText(R.plurals.g5_profile_genre_total_label, genreTotal));
        ViewCompat.setScreenReaderFocusable(view.findViewById(R.id.profile_genre_center), true);
        toggle.setOnClickListener(button -> showAllGenres(chart, counts));
        toggle.setVisibility(counts.size() > GENRE_PREVIEW_SIZE ? View.VISIBLE : View.GONE);
        updateGenreLayout(view.findViewById(R.id.profile_genre_layout));
        renderGenreLegend(legend, chart, counts, Math.min(GENRE_PREVIEW_SIZE, counts.size()), true);
    }

    private void showAllGenres(DonutChartView chart, List<GenreCount> counts) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        ScrollView scroll = new ScrollView(builder.getContext());
        int inset = getResources().getDimensionPixelSize(R.dimen.space_24);
        int titleSpacing = getResources().getDimensionPixelSize(R.dimen.space_8);
        scroll.setPaddingRelative(inset, titleSpacing, inset, 0);

        LinearLayout content = new LinearLayout(builder.getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
        TableLayout legend = new TableLayout(builder.getContext());
        legend.setColumnShrinkable(0, true);
        legend.setColumnStretchable(0, true);
        content.addView(legend, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        renderGenreLegend(legend, chart, counts, counts.size(), false);

        TextView note = new TextView(builder.getContext());
        TextViewCompat.setTextAppearance(note, R.style.TextAppearance_Group6_Caption);
        note.setText(R.string.g5_profile_genre_share_note);
        content.addView(note);
        dialog = builder.setTitle(R.string.g5_profile_all_genres_title)
                .setView(scroll)
                .setPositiveButton(R.string.g5_profile_close_genres, null)
                .show();
    }

    private void renderGenreLegend(TableLayout legend, DonutChartView chart, List<GenreCount> counts,
                                   int visibleCount, boolean insetGenre) {
        legend.removeAllViews();
        addGenreRow(legend, getText(R.string.g5_profile_genre_column),
                getText(R.string.g5_profile_titles_column), getText(R.string.g5_profile_share_column), null, insetGenre);
        View divider = new View(legend.getContext());
        divider.setBackgroundColor(ContextCompat.getColor(legend.getContext(), R.color.outline));
        divider.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        legend.addView(divider, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.card_outline_width)));
        NumberFormat number = NumberFormat.getIntegerInstance(Locale.US);
        for (int i = 0; i < visibleCount; i++) {
            GenreCount count = counts.get(i);
            addGenreRow(legend, count.genre, number.format(count.count),
                    chart.getPercentage(i), chart.getSegmentColor(i), insetGenre);
        }
    }

    private void updateGenreLayout(LinearLayout layout) {
        float widthDp = layout.getWidth() / getResources().getDisplayMetrics().density;
        boolean sideBySide = widthDp >= MIN_SIDE_BY_SIDE_WIDTH_DP
                && getResources().getConfiguration().fontScale <= MAX_SIDE_BY_SIDE_FONT_SCALE;
        int orientation = sideBySide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL;
        if (layout.getOrientation() == orientation && layout.getWidth() > 0) {
            return;
        }
        layout.setOrientation(orientation);
        int chartHeight = getResources().getDimensionPixelSize(R.dimen.poster_height);
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
                sideBySide ? 0 : LinearLayout.LayoutParams.MATCH_PARENT, chartHeight,
                sideBySide ? 2f : 0f);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                sideBySide ? 0 : LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, sideBySide ? 3f : 0f);
        if (sideBySide) {
            int spacing = getResources().getDimensionPixelSize(R.dimen.space_4);
            detailsParams.setMarginStart(spacing);
            detailsParams.setMarginEnd(spacing);
        } else {
            detailsParams.topMargin = getResources().getDimensionPixelSize(R.dimen.space_16);
        }
        layout.findViewById(R.id.profile_chart_container).setLayoutParams(chartParams);
        View details = layout.findViewById(R.id.profile_genre_details);
        details.setLayoutParams(detailsParams);
        details.setTranslationY(sideBySide
                ? getResources().getDimension(R.dimen.space_8) : 0f);
    }

    private void addGenreRow(TableLayout legend, CharSequence name, CharSequence quantity,
                             CharSequence share, Integer color, boolean insetGenre) {
        boolean header = color == null;
        int dotSize = getResources().getDimensionPixelSize(R.dimen.space_8);
        int spacing = getResources().getDimensionPixelSize(R.dimen.space_8);
        int verticalPadding = getResources().getDimensionPixelSize(R.dimen.space_4);
        int textAppearance = header ? R.style.TextAppearance_Group6_Caption
                : R.style.TextAppearance_Group6_Body;

        TableRow row = new TableRow(legend.getContext());
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, verticalPadding, 0, verticalPadding);
        ViewCompat.setAccessibilityHeading(row, header);
        ViewCompat.setScreenReaderFocusable(row, true);

        LinearLayout genreCell = new LinearLayout(row.getContext());
        genreCell.setGravity(Gravity.CENTER_VERTICAL);
        if (insetGenre) {
            genreCell.setPaddingRelative(getResources().getDimensionPixelSize(R.dimen.space_24), 0, 0, 0);
        }
        View dot = new View(row.getContext());
        if (header) {
            dot.setVisibility(View.INVISIBLE);
        } else {
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(color);
            dot.setBackground(circle);
        }
        dot.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dotSize, dotSize);
        dotParams.setMarginEnd(spacing);
        genreCell.addView(dot, dotParams);

        TextView genreLabel = createGenreCell(row, name, textAppearance, Gravity.START, header);
        genreLabel.setSingleLine(true);
        genreLabel.setEllipsize(TextUtils.TruncateAt.END);
        genreCell.addView(genreLabel, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(genreCell, new TableRow.LayoutParams());

        TextView quantityLabel = createGenreCell(row, quantity, textAppearance, Gravity.CENTER_HORIZONTAL, header);
        quantityLabel.setPaddingRelative(spacing / 2, 0, spacing / 2, 0);
        row.addView(quantityLabel, new TableRow.LayoutParams());

        TextView shareLabel = createGenreCell(row, share, textAppearance, Gravity.END, header);
        shareLabel.setPaddingRelative(spacing, 0, 0, 0);
        row.addView(shareLabel, new TableRow.LayoutParams());
        legend.addView(row);
    }

    private TextView createGenreCell(View parent, CharSequence text, int appearance, int gravity, boolean header) {
        TextView cell = new TextView(parent.getContext());
        TextViewCompat.setTextAppearance(cell, appearance);
        cell.setGravity(gravity | Gravity.CENTER_VERTICAL);
        if (header) {
            cell.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
        cell.setText(text);
        return cell;
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
