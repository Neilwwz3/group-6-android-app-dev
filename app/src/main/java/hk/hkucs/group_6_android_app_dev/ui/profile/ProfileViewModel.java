package hk.hkucs.group_6_android_app_dev.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;

import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;
import hk.hkucs.group_6_android_app_dev.data.model.MonthTotal;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;
import hk.hkucs.group_6_android_app_dev.data.repository.SampleDataLoader;
import hk.hkucs.group_6_android_app_dev.ui.components.MonthlyBarChartView;

public class ProfileViewModel extends AndroidViewModel {
    private final MediaRepository repository;
    private final LiveData<StatsSummary> summary;
    private final LiveData<List<GenreCount>> genreCounts;
    private final LiveData<List<MonthTotal>> monthlyTotals;
    private final MediatorLiveData<ProfileHighlights> highlights = new MediatorLiveData<>();
    private List<MediaItem> media;
    private List<RecordWithMedia> records;
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loadFailed = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = MediaRepository.get(application);
        summary = repository.observeSummary();
        genreCounts = repository.observeGenreCounts();
        monthlyTotals = repository.observeMonthlyTotals(
                MonthlyBarChartView.firstDay(Calendar.getInstance()));
        highlights.addSource(repository.observeAllMedia(), items -> {
            media = items;
            highlights.setValue(ProfileHighlights.from(media, records));
        });
        highlights.addSource(repository.observeTimeline(), rows -> {
            records = rows;
            highlights.setValue(ProfileHighlights.from(media, records));
        });
    }

    LiveData<List<MonthTotal>> getMonthlyTotals() {
        return monthlyTotals;
    }

    LiveData<ProfileHighlights> getHighlights() {
        return highlights;
    }

    LiveData<StatsSummary> getSummary() {
        return summary;
    }

    LiveData<List<GenreCount>> getGenreCounts() {
        return genreCounts;
    }

    LiveData<Boolean> getBusy() {
        return busy;
    }

    LiveData<Boolean> getLoadFailed() {
        return loadFailed;
    }

    void acknowledgeLoadFailure() {
        loadFailed.setValue(false);
    }

    void loadSampleData() {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        SampleDataLoader.load(getApplication(), this::finishOperation, error -> {
            finishOperation();
            loadFailed.setValue(true);
        });
    }

    void clearAllData() {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        repository.clearAll(this::finishOperation);
    }

    private void finishOperation() {
        busy.setValue(false);
    }
}
