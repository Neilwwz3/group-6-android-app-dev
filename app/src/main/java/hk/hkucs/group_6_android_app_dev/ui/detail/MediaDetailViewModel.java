package hk.hkucs.group_6_android_app_dev.ui.detail;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;

/** One title plus its watch records. See docs/data-api.md. */
public class MediaDetailViewModel extends AndroidViewModel {

    private final MediaRepository repo;
    private final MutableLiveData<String> mediaId = new MutableLiveData<>();
    private final LiveData<MediaItem> item;
    private final LiveData<List<WatchRecord>> records;

    public MediaDetailViewModel(@NonNull Application application) {
        super(application);
        repo = MediaRepository.get(application);
        item = Transformations.switchMap(mediaId, id -> id == null
                ? new MutableLiveData<MediaItem>() : repo.observeMedia(id));
        records = Transformations.switchMap(mediaId, id -> id == null
                ? new MutableLiveData<List<WatchRecord>>(new ArrayList<WatchRecord>())
                : repo.observeRecords(id));
    }

    public void setMediaId(String id) {
        mediaId.setValue(id);
    }

    /** Null once the title has been deleted. */
    public LiveData<MediaItem> getItem() {
        return item;
    }

    /** Newest first. */
    public LiveData<List<WatchRecord>> getRecords() {
        return records;
    }

    public void setStatus(String status) {
        String id = mediaId.getValue();
        if (id != null) {
            repo.updateStatus(id, status);
        }
    }

    public void setRating(@Nullable Float rating) {
        String id = mediaId.getValue();
        if (id != null) {
            repo.updateRating(id, rating);
        }
    }

    public void delete(@Nullable Runnable onDone) {
        String id = mediaId.getValue();
        if (id != null) {
            repo.deleteMedia(id, onDone);
        }
    }
}
