package hk.hkucs.group_6_android_app_dev.ui.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;

/**
 * Library page data. {@code observeAllMedia} supplies the list; search, filtering and ordering
 * happen here, as documented in docs/data-api.md.
 */
public class LibraryViewModel extends AndroidViewModel {

    /** Chip value meaning "no status filter". */
    public static final String FILTER_ALL = "";

    private final MediatorLiveData<List<MediaItem>> visible = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> statusFilter = new MutableLiveData<>(FILTER_ALL);
    private final MutableLiveData<Boolean> libraryEmpty = new MutableLiveData<>(true);

    private List<MediaItem> all = new ArrayList<>();

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        MediaRepository repo = MediaRepository.get(application);
        visible.addSource(repo.observeAllMedia(), items -> {
            all = items == null ? new ArrayList<MediaItem>() : items;
            libraryEmpty.setValue(all.isEmpty());
            recompute();
        });
        visible.addSource(query, ignored -> recompute());
        visible.addSource(statusFilter, ignored -> recompute());
    }

    /** Filtered list, most recently updated first. */
    public LiveData<List<MediaItem>> getVisible() {
        return visible;
    }

    /** True when the library has no titles at all (as opposed to no filter matches). */
    public LiveData<Boolean> isLibraryEmpty() {
        return libraryEmpty;
    }

    public void setQuery(String value) {
        query.setValue(value == null ? "" : value);
    }

    public void setStatusFilter(String value) {
        statusFilter.setValue(value == null ? FILTER_ALL : value);
    }

    private void recompute() {
        String q = query.getValue() == null
                ? "" : query.getValue().trim().toLowerCase(Locale.ROOT);
        String status = statusFilter.getValue();
        boolean filterStatus = status != null && !FILTER_ALL.equals(status);
        List<MediaItem> result = new ArrayList<>();
        for (MediaItem item : all) {
            if (filterStatus && !status.equals(item.status)) {
                continue;
            }
            if (!q.isEmpty() && !matches(item, q)) {
                continue;
            }
            result.add(item);
        }
        visible.setValue(result);
    }

    private static boolean matches(MediaItem item, String q) {
        return contains(item.title, q) || contains(item.originalTitle, q) || containsGenre(item.genres, q);
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    private static boolean containsGenre(List<String> genres, String q) {
        for (String genre : genres) {
            if (contains(genre, q)) {
                return true;
            }
        }
        return false;
    }
}
