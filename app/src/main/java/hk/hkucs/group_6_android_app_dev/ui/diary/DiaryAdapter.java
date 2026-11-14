package hk.hkucs.group_6_android_app_dev.ui.diary;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hk.hkucs.group_6_android_app_dev.R;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.model.MediaType;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.ui.components.HeatmapView;
import hk.hkucs.group_6_android_app_dev.util.PosterLoader;

/**
 * The whole Diary page as one list: the heatmap card first, then one date header per day with
 * that day's records under it, or an empty-state card when there are no records.
 */
class DiaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface Listener {
        void onDayClick(@NonNull String date, int count);

        void onRecordClick(@NonNull RecordWithMedia record);
    }

    private static final int TYPE_HEATMAP = 0;
    private static final int TYPE_DATE = 1;
    private static final int TYPE_RECORD = 2;
    private static final int TYPE_EMPTY = 3;

    private final Listener listener;
    private List<DiaryViewModel.Row> rows = Collections.emptyList();
    private Map<String, Integer> dailyCounts = Collections.emptyMap();
    @Nullable
    private String selectedDate;
    private boolean loaded;

    DiaryAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    void setRows(@Nullable List<DiaryViewModel.Row> newRows) {
        rows = newRows == null ? Collections.emptyList() : new ArrayList<>(newRows);
        loaded = true;
        notifyDataSetChanged();
    }

    void setDailyCounts(@Nullable Map<String, Integer> counts) {
        dailyCounts = counts == null ? Collections.emptyMap() : counts;
        notifyItemChanged(0);
    }

    void setSelectedDate(@Nullable String date) {
        selectedDate = date;
        notifyItemChanged(0);
    }

    /** Adapter position of a date's header, or -1 when that day has no records. */
    int positionOfDate(@NonNull String date) {
        for (int i = 0; i < rows.size(); i++) {
            DiaryViewModel.Row row = rows.get(i);
            if (row.isHeader() && row.date.equals(date)) {
                return i + 1;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        if (!loaded) {
            return 1;
        }
        return 1 + (rows.isEmpty() ? 1 : rows.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEATMAP;
        }
        if (rows.isEmpty()) {
            return TYPE_EMPTY;
        }
        return rows.get(position - 1).isHeader() ? TYPE_DATE : TYPE_RECORD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_HEATMAP:
                return new HeatmapHolder(inflater.inflate(R.layout.item_diary_header, parent, false));
            case TYPE_DATE:
                return new DateHolder(inflater.inflate(R.layout.item_diary_date, parent, false));
            case TYPE_RECORD:
                return new RecordHolder(inflater.inflate(R.layout.item_diary_record, parent, false));
            default:
                return new RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_diary_empty, parent, false)) {
                };
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeatmapHolder) {
            ((HeatmapHolder) holder).bind(dailyCounts, selectedDate, listener);
        } else if (holder instanceof DateHolder) {
            ((DateHolder) holder).bind(rows.get(position - 1));
        } else if (holder instanceof RecordHolder) {
            RecordWithMedia record = rows.get(position - 1).record;
            if (record != null) {
                ((RecordHolder) holder).bind(record, listener);
            }
        }
    }

    // ---- View holders ----

    static final class HeatmapHolder extends RecyclerView.ViewHolder {
        private final HeatmapView heatmap;
        private final HorizontalScrollView scroll;
        private final TextView summary;
        private final View stats;
        private final TextView longest;
        private final TextView current;
        private final TextView busiest;
        private final View busiestBox;
        private boolean scrolledToToday;

        HeatmapHolder(View view) {
            super(view);
            heatmap = view.findViewById(R.id.diary_heatmap);
            scroll = view.findViewById(R.id.diary_heatmap_scroll);
            summary = view.findViewById(R.id.diary_activity_summary);
            stats = view.findViewById(R.id.diary_stats);
            longest = view.findViewById(R.id.diary_stat_longest);
            current = view.findViewById(R.id.diary_stat_current);
            busiest = view.findViewById(R.id.diary_stat_busiest);
            busiestBox = view.findViewById(R.id.diary_stat_busiest_box);
            addLegendCells(view.getContext(), view.findViewById(R.id.diary_legend_cells));
        }

        void bind(Map<String, Integer> counts, @Nullable String selected, Listener listener) {
            heatmap.setData(counts);
            heatmap.setSelectedDate(selected);
            heatmap.setOnDayClickListener(listener::onDayClick);
            summary.setText(heatmap.getContentDescription());
            bindStats(DiaryStats.from(counts, Calendar.getInstance()), listener);
            if (!scrolledToToday) {
                scrolledToToday = true;
                // Today is in the last column, so start scrolled to the right edge.
                scroll.post(() -> scroll.fullScroll(View.FOCUS_RIGHT));
            }
        }

        private void bindStats(DiaryStats data, Listener listener) {
            Resources res = itemView.getResources();
            String longestText = days(res, data.longestStreak);
            String currentText = days(res, data.currentStreak);
            String busiestText = data.busiestDate == null
                    ? res.getString(R.string.diary_stat_none) : shortDate(data.busiestDate);
            longest.setText(longestText);
            current.setText(currentText);
            busiest.setText(busiestText);
            stats.setContentDescription(res.getString(R.string.diary_stats_description,
                    longestText, currentText, busiestText));
            if (data.busiestDate == null) {
                busiestBox.setOnClickListener(null);
                busiestBox.setClickable(false);
            } else {
                String date = data.busiestDate;
                int count = data.busiestCount;
                busiestBox.setOnClickListener(v -> listener.onDayClick(date, count));
            }
        }

        private static String days(Resources res, int days) {
            return days <= 0 ? res.getString(R.string.diary_stat_none)
                    : res.getQuantityString(R.plurals.diary_stat_days, days, days);
        }

        private static void addLegendCells(Context context, LinearLayout container) {
            Resources res = context.getResources();
            int size = Math.round(12 * res.getDisplayMetrics().density);
            int margin = Math.round(2 * res.getDisplayMetrics().density);
            for (int level = 0; level <= 4; level++) {
                View cell = new View(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                params.setMargins(margin, 0, margin, 0);
                cell.setLayoutParams(params);
                cell.setBackgroundColor(HeatmapView.levelColor(context, level));
                container.addView(cell);
            }
        }
    }

    static final class DateHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView count;

        DateHolder(View view) {
            super(view);
            date = view.findViewById(R.id.diary_date);
            count = view.findViewById(R.id.diary_date_count);
        }

        void bind(DiaryViewModel.Row row) {
            date.setText(longDate(row.date));
            count.setText(itemView.getResources().getQuantityString(
                    R.plurals.diary_day_entries, row.dayCount, row.dayCount));
        }
    }

    static final class RecordHolder extends RecyclerView.ViewHolder {
        private final TextView poster;
        private final ImageView posterImage;
        private final TextView title;
        private final TextView meta;
        private final TextView rating;
        private final TextView note;

        RecordHolder(View view) {
            super(view);
            poster = view.findViewById(R.id.diary_record_poster);
            posterImage = view.findViewById(R.id.diary_record_poster_image);
            title = view.findViewById(R.id.diary_record_title);
            meta = view.findViewById(R.id.diary_record_meta);
            rating = view.findViewById(R.id.diary_record_rating);
            note = view.findViewById(R.id.diary_record_note);
        }

        void bind(RecordWithMedia item, Listener listener) {
            Resources res = itemView.getResources();
            WatchRecord record = item.record;
            String mediaTitle = item.mediaTitle == null ? "" : item.mediaTitle;

            title.setText(mediaTitle);
            poster.setText(mediaTitle.isEmpty() ? "" : mediaTitle.substring(0, 1).toUpperCase(Locale.ENGLISH));
            PosterLoader.load(itemView.getContext(), item.mediaPoster, posterImage, poster);

            String metaText = metaLine(res, item);
            meta.setText(metaText);
            meta.setVisibility(metaText.isEmpty() ? View.GONE : View.VISIBLE);

            if (record.rating == null) {
                rating.setVisibility(View.GONE);
            } else {
                String score = formatScore(record.rating);
                rating.setVisibility(View.VISIBLE);
                rating.setText(res.getString(R.string.diary_rating, score));
                rating.setContentDescription(res.getString(R.string.diary_rating_description, score));
            }

            note.setText(record.note);
            note.setVisibility(record.note.trim().isEmpty() ? View.GONE : View.VISIBLE);

            itemView.setOnClickListener(v -> listener.onRecordClick(item));
        }
    }

    // ---- Formatting ----

    static String longDate(String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate);
            return new SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH).format(date);
        } catch (ParseException | NullPointerException e) {
            return isoDate;
        }
    }

    /** {@code 2026-02-22} as {@code Feb 22}. */
    static String shortDate(String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate);
            return new SimpleDateFormat("MMM d", Locale.ENGLISH).format(date);
        } catch (ParseException | NullPointerException e) {
            return isoDate;
        }
    }

    static String formatScore(float score) {
        return score == Math.rint(score) ? String.valueOf((int) score)
                : String.format(Locale.ENGLISH, "%.1f", score);
    }

    private static String metaLine(Resources res, RecordWithMedia item) {
        List<String> parts = new ArrayList<>();
        WatchRecord record = item.record;
        if (MediaType.SERIES.equals(item.mediaType) && !record.episodes.isEmpty()) {
            parts.add(episodes(res, record.episodes));
        }
        if (record.minutes != null && record.minutes > 0) {
            int hours = record.minutes / 60;
            int minutes = record.minutes % 60;
            parts.add(hours > 0 ? res.getString(R.string.diary_hours_minutes, hours, minutes)
                    : res.getString(R.string.diary_minutes, minutes));
        }
        return android.text.TextUtils.join(res.getString(R.string.diary_meta_separator), parts);
    }

    private static String episodes(Resources res, List<Integer> episodes) {
        if (episodes.size() == 1) {
            return res.getString(R.string.diary_episode_one, episodes.get(0));
        }
        List<Integer> sorted = new ArrayList<>(episodes);
        Collections.sort(sorted);
        boolean consecutive = true;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i) != sorted.get(i - 1) + 1) {
                consecutive = false;
                break;
            }
        }
        if (consecutive) {
            return res.getString(R.string.diary_episode_range, sorted.get(0),
                    sorted.get(sorted.size() - 1));
        }
        return res.getString(R.string.diary_episode_list, android.text.TextUtils.join(", ", sorted));
    }
}
