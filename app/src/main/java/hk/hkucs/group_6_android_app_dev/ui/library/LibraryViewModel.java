package hk.hkucs.group_6_android_app_dev.ui.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;

/**
 * Library page data. {@code observeAllMedia} supplies the list; search, filtering and ordering
 * happen here, as documented in docs/data-api.md.
 */
public class LibraryViewModel extends AndroidViewModel {

    /** Chip value meaning "no status filter". */
    public static final String FILTER_ALL = "";

    /** Sort orders. Recently updated is the repository's own order. */
    public static final int SORT_RECENT = 0;
    public static final int SORT_RATING = 1;
    public static final int SORT_TITLE = 2;
    public static final int SORT_YEAR = 3;

    private final MediatorLiveData<List<MediaItem>> visible = new MediatorLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MutableLiveData<String> statusFilter = new MutableLiveData<>(FILTER_ALL);
    private final MutableLiveData<Boolean> libraryEmpty = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> sort = new MutableLiveData<>(SORT_RECENT);
    private final MutableLiveData<Map<String, Integer>> statusCounts =
            new MutableLiveData<>(new HashMap<>());

    private List<MediaItem> all = new ArrayList<>();

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        MediaRepository repo = MediaRepository.get(application);
        visible.addSource(repo.observeAllMedia(), items -> {
            all = items == null ? new ArrayList<MediaItem>() : items;
            libraryEmpty.setValue(all.isEmpty());
            statusCounts.setValue(countByStatus(all));
            recompute();
        });
        visible.addSource(query, ignored -> recompute());
        visible.addSource(statusFilter, ignored -> recompute());
        visible.addSource(sort, ignored -> recompute());
    }

    /** Number of titles per status, plus {@link #FILTER_ALL} for the whole library. */
    public LiveData<Map<String, Integer>> getStatusCounts() {
        return statusCounts;
    }

    public LiveData<Integer> getSort() {
        return sort;
    }

    public void setSort(int value) {
        sort.setValue(value);
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
        Integer order = sort.getValue();
        visible.setValue(sorted(result, order == null ? SORT_RECENT : order));
    }

    private static Map<String, Integer> countByStatus(List<MediaItem> items) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put(FILTER_ALL, items.size());
        for (MediaItem item : items) {
            Integer old = counts.get(item.status);
            counts.put(item.status, old == null ? 1 : old + 1);
        }
        return counts;
    }

    /** A sorted copy. Unrated titles and titles without a year go last; ties keep their order. */
    @VisibleForTesting
    static List<MediaItem> sorted(List<MediaItem> items, int order) {
        List<MediaItem> result = new ArrayList<>(items);
        Comparator<MediaItem> comparator;
        switch (order) {
            case SORT_RATING:
                comparator = (a, b) -> compareDescNullsLast(a.rating, b.rating);
                break;
            case SORT_TITLE:
                comparator = (a, b) -> a.title.compareToIgnoreCase(b.title);
                break;
            case SORT_YEAR:
                comparator = (a, b) -> compareDescNullsLast(a.year, b.year);
                break;
            default:
                return result;
        }
        Collections.sort(result, comparator);
        return result;
    }

    private static <T extends Comparable<T>> int compareDescNullsLast(T a, T b) {
        if (a == null || b == null) {
            return a == null ? (b == null ? 0 : 1) : -1;
        }
        return b.compareTo(a);
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
