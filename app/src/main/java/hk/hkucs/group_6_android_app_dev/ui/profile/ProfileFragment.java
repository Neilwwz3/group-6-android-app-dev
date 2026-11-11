package hk.hkucs.group_6_android_app_dev.ui.profile;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;
import hk.hkucs.group_6_android_app_dev.ui.components.DonutChartView;

public class ProfileFragment extends Fragment {
    private AlertDialog dialog;

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
        setText(view, R.id.profile_titles, getString(R.string.g5_profile_titles, summary.mediaCount));
        setText(view, R.id.profile_watched, getString(R.string.g5_profile_watched, summary.watchedCount));
        setText(view, R.id.profile_time, getString(R.string.g5_profile_time, summary.totalMinutes));
        String rating = summary.averageRating == null
                ? getString(R.string.g5_profile_no_rating)
                : getString(R.string.g5_profile_rating, summary.averageRating);
        setText(view, R.id.profile_average, rating);
    }

    private void renderGenres(View view, List<GenreCount> counts) {
        DonutChartView chart = view.findViewById(R.id.profile_chart);
        boolean hasGenres = counts != null && !counts.isEmpty();
        chart.setVisibility(hasGenres ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.profile_no_genres).setVisibility(hasGenres ? View.GONE : View.VISIBLE);
        if (!hasGenres) {
            setText(view, R.id.profile_legend, "");
            return;
        }

        chart.setCounts(counts);
        SpannableStringBuilder legend = new SpannableStringBuilder();
        for (int i = 0; i < counts.size(); i++) {
            if (i > 0) {
                legend.append('\n');
            }
            GenreCount count = counts.get(i);
            int rowStart = legend.length();
            legend.append(getString(R.string.g5_profile_genre_row, count.genre, count.count));
            legend.setSpan(new ForegroundColorSpan(chart.getSegmentColor(i)), rowStart, rowStart + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setText(view, R.id.profile_legend, legend);
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
