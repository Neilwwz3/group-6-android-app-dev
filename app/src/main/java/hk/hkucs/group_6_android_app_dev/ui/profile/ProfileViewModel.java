package hk.hkucs.group_6_android_app_dev.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.model.GenreCount;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;
import hk.hkucs.group_6_android_app_dev.data.repository.SampleDataLoader;

public class ProfileViewModel extends AndroidViewModel {
    private final MediaRepository repository;
    private final LiveData<StatsSummary> summary;
    private final LiveData<List<GenreCount>> genreCounts;
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loadFailed = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = MediaRepository.get(application);
        summary = repository.observeSummary();
        genreCounts = repository.observeGenreCounts();
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
