package hk.hkucs.group_6_android_app_dev.ui.record;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;

public class AddRecordViewModel extends AndroidViewModel {

    enum SaveState {
        READY,
        SAVING,
        SAVED
    }

    private final MediaRepository repository;
    private final MutableLiveData<SaveState> saveState =
            new MutableLiveData<>(SaveState.READY);

    public AddRecordViewModel(@NonNull Application application) {
        super(application);
        repository = MediaRepository.get(application);
    }

    LiveData<SaveState> getSaveState() {
        return saveState;
    }

    LiveData<MediaItem> observeMedia(String mediaId) {
        return repository.observeMedia(mediaId);
    }

    void save(String mediaId, String date, @Nullable Integer minutes,
              @Nullable Float rating, String note, List<Integer> episodes) {
        if (saveState.getValue() != SaveState.READY) {
            return;
        }

        WatchRecord record = new WatchRecord();
        record.mediaId = mediaId;
        record.date = date;
        record.minutes = minutes;
        record.rating = rating;
        record.note = note;
        record.episodes = new ArrayList<>(episodes);

        saveState.setValue(SaveState.SAVING);
        repository.addRecord(record, () -> saveState.setValue(SaveState.SAVED));
    }
}
