package hk.hkucs.group_6_android_app_dev.ui.edit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.function.Consumer;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;

/** Loads the title being edited and saves it back. See docs/data-api.md. */
public class EditMediaViewModel extends AndroidViewModel {

    private final MediaRepository repo;
    private final MutableLiveData<String> mediaId = new MutableLiveData<>();
    private final LiveData<MediaItem> item;

    public EditMediaViewModel(@NonNull Application application) {
        super(application);
        repo = MediaRepository.get(application);
        item = Transformations.switchMap(mediaId, id -> id == null
                ? new MutableLiveData<MediaItem>() : repo.observeMedia(id));
    }

    public void setMediaId(@Nullable String id) {
        mediaId.setValue(id);
    }

    /** Null in add mode; the stored item in edit mode. */
    public LiveData<MediaItem> getItem() {
        return item;
    }

    public void save(MediaItem item, @Nullable Consumer<String> onSaved) {
        repo.saveMedia(item, onSaved);
    }
}
