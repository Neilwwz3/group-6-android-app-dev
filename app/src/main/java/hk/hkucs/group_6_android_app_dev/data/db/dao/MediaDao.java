package hk.hkucs.group_6_android_app_dev.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import hk.hkucs.group_6_android_app_dev.data.db.entity.MediaItem;
import hk.hkucs.group_6_android_app_dev.data.model.StatsSummary;

@Dao
public interface MediaDao {

    @Query("SELECT * FROM media_item ORDER BY updated_at DESC")
    LiveData<List<MediaItem>> observeAll();

    @Query("SELECT * FROM media_item WHERE id = :id")
    LiveData<MediaItem> observeById(String id);

    @Query("SELECT * FROM media_item WHERE status = :status ORDER BY updated_at DESC")
    LiveData<List<MediaItem>> observeByStatus(String status);

    @Query("SELECT * FROM media_item WHERE id = :id")
    MediaItem getById(String id);

    /** Insert or update without deleting the row, so its watch records survive. */
    @Upsert
    void upsert(MediaItem item);

    @Insert
    void insertAll(List<MediaItem> items);

    @Query("UPDATE media_item SET status = :status, updated_at = :now WHERE id = :id")
    void updateStatus(String id, String status, long now);

    @Query("UPDATE media_item SET rating = :rating, updated_at = :now WHERE id = :id")
    void updateRating(String id, Float rating, long now);

    @Query("DELETE FROM media_item WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM media_item")
    void deleteAll();

    @Query("SELECT "
            + "(SELECT COUNT(*) FROM media_item) AS mediaCount, "
            + "(SELECT COUNT(*) FROM media_item WHERE status = 'watched') AS watchedCount, "
            + "(SELECT IFNULL(SUM(minutes), 0) FROM watch_record) AS totalMinutes, "
            + "(SELECT AVG(rating) FROM media_item WHERE rating IS NOT NULL) AS averageRating, "
            + "(SELECT COUNT(*) FROM watch_record) AS recordCount")
    LiveData<StatsSummary> observeSummary();
}
