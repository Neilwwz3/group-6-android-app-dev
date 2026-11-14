package hk.hkucs.group_6_android_app_dev.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.db.entity.WatchRecord;
import hk.hkucs.group_6_android_app_dev.data.model.DayCount;
import hk.hkucs.group_6_android_app_dev.data.model.MonthTotal;
import hk.hkucs.group_6_android_app_dev.data.model.RecordWithMedia;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;

@Dao
public interface WatchRecordDao {

    String WITH_MEDIA = "SELECT r.*, m.title AS media_title, m.poster AS media_poster, "
            + "m.type AS media_type FROM watch_record r JOIN media_item m ON m.id = r.media_id ";

    @Insert
    long insert(WatchRecord record);

    @Insert
    void insertAll(List<WatchRecord> records);

    @Query("SELECT * FROM watch_record WHERE media_id = :mediaId ORDER BY date DESC, created_at DESC")
    LiveData<List<WatchRecord>> observeForMedia(String mediaId);

    @Query(WITH_MEDIA + "ORDER BY r.date DESC, r.created_at DESC")
    LiveData<List<RecordWithMedia>> observeTimeline();

    @Query(WITH_MEDIA + "ORDER BY r.date DESC, r.created_at DESC LIMIT :limit")
    LiveData<List<RecordWithMedia>> observeRecent(int limit);

    @Query("SELECT date, COUNT(*) AS count FROM watch_record "
            + "WHERE date BETWEEN :fromDate AND :toDate GROUP BY date")
    LiveData<List<DayCount>> observeDailyCounts(String fromDate, String toDate);

    @Query("SELECT date, COUNT(*) AS count FROM watch_record "
            + "WHERE date BETWEEN :fromDate AND :toDate GROUP BY date")
    List<DayCount> getDailyCounts(String fromDate, String toDate);

    @Query("SELECT "
            + "COUNT(DISTINCT r.media_id) AS mediaCount, "
            + "COUNT(DISTINCT CASE WHEN m.status = 'watched' THEN r.media_id END) AS watchedCount, "
            + "IFNULL(SUM(r.minutes), 0) AS totalMinutes, "
            + "AVG(r.rating) AS averageRating, "
            + "COUNT(*) AS recordCount "
            + "FROM watch_record r JOIN media_item m ON m.id = r.media_id "
            + "WHERE r.date LIKE :yearMonth || '-%'")
    LiveData<StatsSummary> observeMonthSummary(String yearMonth);

    @Query("SELECT substr(date, 1, 7) AS month, COUNT(*) AS count, "
            + "IFNULL(SUM(minutes), 0) AS minutes FROM watch_record "
            + "WHERE date >= :fromDate GROUP BY month ORDER BY month")
    LiveData<List<MonthTotal>> observeMonthlyTotals(String fromDate);

    @Query("DELETE FROM watch_record WHERE media_id = :mediaId")
    void deleteForMedia(String mediaId);

    @Query("DELETE FROM watch_record")
    void deleteAll();
}
