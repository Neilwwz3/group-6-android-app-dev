package hk.hkucs.group_6_android_app_dev.ui.diary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;
import hk.hkucs.group_6_android_app_dev.ui.components.HeatmapView;

/** Heatmap counts and timeline rows for the Diary tab. */
public class DiaryViewModel extends AndroidViewModel {

    /** One row of the timeline: a date header or a watch record under it. */
    public static final class Row {
        public final String date;
        /** Records on this date; only set on header rows. */
        public final int dayCount;
        /** Null for header rows. */
        @Nullable
        public final RecordWithMedia record;

        private Row(String date, int dayCount, @Nullable RecordWithMedia record) {
            this.date = date;
            this.dayCount = dayCount;
            this.record = record;
        }

        public boolean isHeader() {
            return record == null;
        }
    }

    private final LiveData<Map<String, Integer>> dailyCounts;
    private final LiveData<List<Row>> rows;
    private final MutableLiveData<String> selectedDate = new MutableLiveData<>();

    public DiaryViewModel(@NonNull Application application) {
        super(application);
        MediaRepository repo = MediaRepository.get(application);
        Calendar today = Calendar.getInstance();
        dailyCounts = repo.observeDailyCounts(HeatmapView.firstDay(today),
                HeatmapView.format(today));
        rows = Transformations.map(repo.observeTimeline(), DiaryViewModel::toRows);
    }

    public LiveData<Map<String, Integer>> getDailyCounts() {
        return dailyCounts;
    }

    public LiveData<List<Row>> getRows() {
        return rows;
    }

    /** The day picked on the heatmap; survives rotation. */
    public LiveData<String> getSelectedDate() {
        return selectedDate;
    }

    public void selectDate(@Nullable String date) {
        selectedDate.setValue(date);
    }

    /** Groups records (newest first) under one header per date. */
    @VisibleForTesting
    static List<Row> toRows(@Nullable List<RecordWithMedia> records) {
        List<Row> result = new ArrayList<>();
        if (records == null) {
            return result;
        }
        int i = 0;
        while (i < records.size()) {
            String date = records.get(i).record.date;
            int end = i;
            while (end < records.size() && date.equals(records.get(end).record.date)) {
                end++;
            }
            result.add(new Row(date, end - i, null));
            for (int j = i; j < end; j++) {
                result.add(new Row(date, 0, records.get(j)));
            }
            i = end;
        }
        return result;
    }
}
