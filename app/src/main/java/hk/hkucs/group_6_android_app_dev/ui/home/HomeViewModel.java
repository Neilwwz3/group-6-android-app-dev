package hk.hkucs.group_6_android_app_dev.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.model.MediaStatus;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;
import hk.hkucs.group_6_android_app_dev.data.repository.MediaRepository;

public class HomeViewModel extends AndroidViewModel {
    final LiveData<List<MediaItem>> watching;
    final LiveData<List<RecordWithMedia>> recent;
    final LiveData<StatsSummary> monthSummary;
    final LiveData<StatsSummary> summary;
    final LiveData<String> selectedMonth;
    private final MutableLiveData<String> month = new MutableLiveData<>();
    private final String currentMonth;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        MediaRepository repository = MediaRepository.get(application);
        watching = repository.observeMediaByStatus(MediaStatus.WATCHING);
        recent = repository.observeRecentRecords(5);
        monthSummary = Transformations.switchMap(month, repository::observeMonthSummary);
        summary = repository.observeSummary();
        selectedMonth = month;
        currentMonth = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        month.setValue(currentMonth);
    }

    void selectMonth(int year, int oneBasedMonth) {
        month.setValue(monthKey(year, oneBasedMonth));
    }

    void moveMonth(int offset) {
        String value = month.getValue();
        if (value == null || offset == 0) {
            return;
        }
        String next = shiftMonth(value, offset);
        if (next.compareTo(currentMonth) <= 0) {
            month.setValue(next);
        }
    }

    String getSelectedMonth() {
        return month.getValue() == null ? currentMonth : month.getValue();
    }

    boolean canMoveNext() {
        return getSelectedMonth().compareTo(currentMonth) < 0;
    }

    static String monthKey(int year, int oneBasedMonth) {
        int safeMonth = Math.max(1, Math.min(12, oneBasedMonth));
        return String.format(Locale.US, "%04d-%02d", year, safeMonth);
    }

    static String shiftMonth(String value, int offset) {
        String[] parts = value.split("-");
        if (parts.length != 2) {
            return value;
        }
        try {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
            calendar.clear();
            calendar.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, 1);
            calendar.add(Calendar.MONTH, offset);
            return monthKey(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        } catch (NumberFormatException exception) {
            return value;
        }
    }
}
